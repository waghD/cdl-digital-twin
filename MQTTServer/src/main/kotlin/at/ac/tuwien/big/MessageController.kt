package at.ac.tuwien.big

import at.ac.tuwien.big.entity.state.*
import at.ac.tuwien.big.entity.transition.RoboticArmTransition
import at.ac.tuwien.big.entity.transition.Transition
import com.github.sarxos.webcam.Webcam
import com.google.gson.Gson
import java.awt.Dimension
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import kotlin.concurrent.schedule


class MessageController(private val mqtt: MQTT,
                        private val objectTracker: ObjectTracker,
                        private val timeSeriesDatabase: TimeSeriesDatabase) {

    private val gson = Gson()
    private val timer = Timer()
    private val subscribers = mutableListOf<(String, String) -> Unit>()
    var messageRate: Int = 10
    var autoPlay: Boolean = false
    var recording: Boolean = false
    var lastInTransition = false
    var webcam: Webcam?

    init {
        mqtt.subscribe(listOf(simSensor, sensor, detectionCamera, pickupCamera, status), this::onMessage)
        StateObserver.subscribe {
            StateObserver.latestMatches.forEach {it2 ->
                if (it2.value.first == it) {
                    sendWebSocketMessageState("{\"machine\":\"" + it2.key + "\",\"name\":\"" + it.name + "\"}")
                }

            }
        }
        try {
            webcam = Webcam.getWebcams()[1]
            webcam?.viewSize = Dimension(640, 480)
        } catch (e: IndexOutOfBoundsException) {
            webcam = null
            println("No web cam detected")
        }
    }

    fun subscribe(callback: (String, String) -> Unit) {
        subscribers.add(callback)
    }

    fun start() {
        timer.schedule(0, 1000) {
            observe()
        }

        println("Is webcam null in MessageController?")
        println(webcam)
        if (webcam != null) {
            timer.schedule(0, 1000) {
                webcam?.open()
                val file = File.createTempFile("webcam", ".png")
                val stream = ByteArrayOutputStream()
                val img = webcam?.image?.getSubimage(320, 220, 200, 200)
                ImageIO.write(img, "PNG", file)
                ImageIO.write(img, "PNG", stream)
                webcam?.close()
                objectTracker.track(file) {
                    val tracking = it.firstOrNull()
                    val detected = tracking != null
                    val inPickupWindow = tracking != null && 36 < tracking.x && tracking.x < 125 && 60 < tracking.y && tracking.y < 105
                    StateObserver.cameraState.pickupDetected = detected
                    StateObserver.cameraState.testRigDetected = inPickupWindow
                    val base64 = Base64.getEncoder().encodeToString(stream.toByteArray())
                    sendWebSocketMessagePickupCamera("{\"image\": \"$base64\", \"tracking\": ${gson.toJson(it)}}")
                    file.delete()
                }
            }
        }
    }

    /**
     *
     */
    private fun onMessage(topic: String, message: String) {
        when (topic) {
            status -> {
                sendWebSocketMessageContext(message)
            }

            sensor, simSensor -> {

                val state = parse(message)
                if (recording) {
                    if (state is RoboticArmState) {
                        val label = if (lastInTransition) null else StateObserver.targetStates["roboticArm"]?.name
                        timeSeriesDatabase.savePoint(state, label)
                    }
                }

                StateObserver.update(state)
                sendWebSocketMessageSensor(gson.toJson(state))
            }
            detectionCamera -> {
                val code = QRCode.read(message)
                if (code != null) {
                    sendWebSocketMessageQRCodeScanner(gson.toJson(code).toString())
                }
                val color = if (code == null) ObjectCategory.NONE else if (code.color == "red") ObjectCategory.RED else ObjectCategory.GREEN
                StateObserver.update(TestingRigState(objectCategory = color))

                val detection = File.createTempFile("detection", ".png")
                detection.writeBytes(fromBase64(message))
                objectTracker.track(detection) {
                    sendWebSocketMessageDetectionCamera("{\"image\": \"$message\", \"tracking\": ${gson.toJson(it)}}")
                    detection.delete()
                }
            }
            pickupCamera -> {
                val pickup = File.createTempFile("pickup", ".png")
                pickup.writeBytes(fromBase64(message))
                objectTracker.track(pickup) {
                    val tracking = it.firstOrNull()
                    val detected = tracking != null
                    val inPickupWindow = tracking != null && 36 < tracking.x && tracking.x < 125 && 60 < tracking.y && tracking.y < 105
                    StateObserver.cameraState.pickupDetected = detected
                    StateObserver.cameraState.testRigDetected = inPickupWindow

                    sendWebSocketMessagePickupCamera("{\"image\": \"$message\", \"tracking\": ${gson.toJson(it)}}")
                    pickup.delete()
                }
            }
        }
    }

    private fun observe() {
        if (autoPlay) {
            val transitions = mutableListOf<Transition>()
            StateObserver.stateMachines?.forEach {
                if (!StateObserver.atEndState(it)) {
                    transitions.addAll(StateObserver.next(it))
                } else {
                    transitions.addAll(StateObserver.reset(it))
                }
            }

            val latest = StateObserver.latestMatches["roboticArm"]?.first
            val formerTarget = StateObserver.targetStates["roboticArm"]
            /*val transitions = if (!StateObserver.atEndState()) {
                StateObserver.next()
            } else {
                StateObserver.reset()
            }*/
           // println("Transitions:")
           // println(transitions)

            val nowInTransition = formerTarget?.environment?.roboticArmState != StateObserver.targetStates["roboticArm"]?.environment?.roboticArmState
            if (transitions.isNotEmpty()) {
                for (transition in transitions) {

                    if (transition is RoboticArmTransition) {
                        if (!lastInTransition && nowInTransition) {
                            // println("Next: ${latest.name} -> ${StateObserver.targetState.name}")
                        }
                        if (lastInTransition && !nowInTransition) {
                        }
                    }
                    StateObserver.latestMatches.forEach {
                        sendWebSocketMessageContext(gson.toJson(it.value))


                    }
                   // val context = StateObserver.latestMatch
                    //sendWebSocketMessageContext(gson.toJson(context))
                    val commands = StateObserver.transform(transition)
                    for (c in commands) {

                        mqtt.send(c)
                    }
                }
            }
            lastInTransition = nowInTransition
        }
    }

    fun reset() {

       // val latest = StateObserver.latestMatch.first
       // val formerTarget = StateObserver.targetStates["roboticArm"]
        val transitions = mutableListOf<Transition>()
        StateObserver.stateMachines?.forEach {
            transitions.addAll(StateObserver.reset(it))
        }

       // val nowInTransition = formerTarget?.environment?.roboticArmState != StateObserver.targetStates["roboticArm"]?.environment?.roboticArmState
        println(StateObserver.latestMatches)
        for (transition in transitions) {
            /*if (!lastInTransition && nowInTransition) {
            }*/
            StateObserver.latestMatches.forEach {
                sendWebSocketMessageContext(gson.toJson( it.value))
            }

            //val context = StateObserver.latestMatch

            //sendWebSocketMessageContext(gson.toJson(context))
            val commands = StateObserver.transform(transition)


            for (c in commands) {

                mqtt.send(c)
            }
        }
        //lastInTransition = nowInTransition
    }


    private fun parse(payload: String): StateEvent {
        val basicState = gson.fromJson(payload, BasicStateEvent::class.java)
        return when (basicState.entity) {
            "RoboticArm" -> gson.fromJson(payload, RoboticArmState::class.java)
            "Slider" -> gson.fromJson(payload, SliderState::class.java)
            "Conveyor" -> gson.fromJson(payload, ConveyorState::class.java)
            "TestingRig" -> gson.fromJson(payload, TestingRigState::class.java)
            "Gate" -> gson.fromJson(payload, GatePassed::class.java)
            else -> BasicStateEvent()
        }
    }

    private fun sendWebSocketMessageSensor(data: String) {
        sendWebSocketMessage("sensor", data)
    }

    private fun sendWebSocketMessageState(data: String) {
        sendWebSocketMessage("state", data)
    }

    private fun sendWebSocketMessageContext(data: String) {
        sendWebSocketMessage("context", data)
    }

    private fun sendWebSocketMessagePickupCamera(data: String) {
        sendWebSocketMessage("pickupCamera", data)
    }

    private fun sendWebSocketMessageDetectionCamera(data: String) {
        sendWebSocketMessage("detectionCamera", data)
    }

    private fun sendWebSocketMessageQRCodeScanner(data: String) {
        sendWebSocketMessage("qrCode", data)
    }

    private fun sendWebSocketMessage(topic: String, data: String) {
        subscribers.forEach {
            it(topic, data)
        }
    }
}
