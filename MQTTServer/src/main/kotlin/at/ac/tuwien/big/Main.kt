@file:JvmName("MainKt")

package at.ac.tuwien.big

import at.ac.tuwien.big.api.HedgehogController
import at.ac.tuwien.big.api.JobController
import at.ac.tuwien.big.api.WebController
import at.ac.tuwien.big.sm.BasicState

/**
 * Set of hosts required for all services
 */
data class HostConfig(
        val influx: String,
        val mqtt: String,
        val objectTracker: String)

val default = HostConfig("127.0.0.1", "127.0.0.1", "127.0.0.1")
val docker = HostConfig("influx", "mqtt", "object-tracker")

const val simSensor = "Sensor-Simulation"
const val sensor = "Sensor"
const val detectionCamera = "DetectionCamera"
const val pickupCamera = "PickupCamera"
const val simActuator = "Actuator-Simulation"
const val actuator = "Actuator"
const val status = "Status"

fun main(args: Array<String>) {
    Runtime.getRuntime().addShutdownHook(Thread {
        if (HedgehogController.hasStarted()) {
            HedgehogController.stop()
        }
    })
    //HedgehogController.start()
    val hosts = if (args.firstOrNull() == "--docker") {
        docker
    } else {
        default

    }
    val sensors = listOf(simSensor, sensor, detectionCamera, pickupCamera)
    val actuators = listOf(simActuator, actuator)
    val status = listOf(status)
    val objectTracker = ObjectTracker(hosts.objectTracker)
    val influx = TimeSeriesDatabase(hosts.influx)
    val mqtt = MQTT(hosts.mqtt, sensors + status, actuators)
    val controller = MessageController(mqtt, objectTracker, influx)
    val jobs = JobController()
    val web = WebController(mqtt, controller, jobs, influx)
    StateObserver.stateMachines = mutableListOf(
            StateMachine("roboticArm", jobs.getJobs().first().roboticArmStates),
            StateMachine("conveyor",jobs.getJobs().first().conveyorStates),
            StateMachine("testingRig",jobs.getJobs().first().testingRigStates),
            StateMachine("slider",jobs.getJobs().first().sliderStates)
    )

    /*StateObserver.stateMachines!!.forEach { sm ->
        StateObserver.latestMatches[sm.name] = Pair(sm.states.first() as BasicState, true)
    }*/


    controller.start()
    web.start()
}
