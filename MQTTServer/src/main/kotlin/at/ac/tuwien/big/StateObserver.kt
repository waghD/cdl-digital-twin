package at.ac.tuwien.big

import at.ac.tuwien.big.entity.state.*
import at.ac.tuwien.big.entity.transition.*
import at.ac.tuwien.big.sm.BasicState

/**
 * This object maintains the current state of the environment and tries to match the current state to states defined
 * in the job. By finding successor states to currently matched states, the system follows the control procedure.
 * Internally, the object uses [StateMachine] to find the correct successor states.
 */
object StateObserver : Observable<BasicState>() {

    /**
     * State machine used to find correct successor states
     */
    var stateMachines: MutableList<StateMachine>? = null

    /*
     * Current camera states in environments for detection of objects
     * on conveyor and test rig
     */
    var cameraState: CameraState = CameraState()

    /**
     * Most recently observed state
     */
    private var snapshot: Environment = Environment(RoboticArmState(), SliderState(), ConveyorState(), TestingRigState())

    /**
     * Latest matching state, given the current job
     */
    var latestMatches: MutableMap<String, Pair<BasicState, Boolean>> = mutableMapOf(
                                                "roboticArm" to Pair(BasicState(), true),
                                                "conveyor" to Pair(BasicState(), true),
                                                "testingRig" to Pair(BasicState(), true),
                                                "slider" to Pair(BasicState(), true)
                                            )
        private set

    /**
     * Successor state, given the latest matching state
     */
    var targetStates:  MutableMap<String, BasicState> = mutableMapOf(
            "roboticArm" to BasicState(),
            "conveyor" to BasicState(),
            "testingRig" to BasicState(),
            "slider" to BasicState()
    )


    /**
     * Update the unit with new sensor information. This includes matching the updated state to the set of defined states.
     *
     * @param e: StateEvent holding current sensor information
     */
    fun update(e: StateEvent) {
        when (e) {
            is RoboticArmState -> {
                snapshot = snapshot.copy(roboticArmState = e)
            }
            is SliderState -> {
                snapshot = snapshot.copy(sliderState = e)
            }
            is ConveyorState -> {
               /* val c = snapshot.conveyorState ?: ConveyorState()
                val cNew = c.copy(
                        adjusterPosition = e.adjusterPosition ?: c.adjusterPosition,
                        detected = e.detected ?: c.detected,
                        inPickupWindow = e.inPickupWindow ?: c.inPickupWindow)*/
                snapshot = snapshot.copy(conveyorState = e)
            }
            is TestingRigState -> {
                val t = snapshot.testingRigState ?: TestingRigState()
                val tNew = t.copy(
                        objectCategory = e.objectCategory ?: t.objectCategory,
                        platformPosition = e.platformPosition ?: t.platformPosition,
                        heatplateTemperature = e.heatplateTemperature ?: t.heatplateTemperature)
                snapshot = snapshot.copy(testingRigState = tNew)
            }
        }



        StateObserver.stateMachines?.forEach {sm ->
            val match = matchState(snapshot, sm)
            if (match != null && latestMatches[sm.name] != match ) {
                /* Check if this newly matched stated satisfies all constraints assigned to it
                * If satisfied, can transition into this new state */
                if ( match.first.isSatisfied(latestMatches, cameraState)) {
                    latestMatches[sm.name] = match
                    notify(latestMatches[sm.name]?.first ?: BasicState())
                }

            }
        }

    }



    /**
     * Return the defined successor state of the latest matching state, according to the state machine
     */
    fun next(stateMachine: StateMachine): List<Transition> {
        val successor = stateMachine?.successor(latestMatches[stateMachine.name]?.first ?: BasicState(), latestMatches[stateMachine.name]?.second)
        return if (successor != null) {
            targetStates[stateMachine.name] = successor
            val isSecond = latestMatches[stateMachine.name]?.second ?: false

            val env = if (isSecond) {
                latestMatches[stateMachine.name]?.first?.environment
            } else {
                latestMatches[stateMachine.name]?.first?.altEnvironment!!
            }

            val succ = successor.environment

            val result = mutableListOf<Transition>()

            when (stateMachine.name) {
                "roboticArm" -> result.add(RoboticArmTransition(env?.roboticArmState ?: RoboticArmState(), succ.roboticArmState ?: RoboticArmState()))
                "conveyor" -> result.add(ConveyorTransition(env?.conveyorState ?: ConveyorState(), succ.conveyorState ?: ConveyorState()))
                "testingRig" -> result.add(TestingRigTransition(env?.testingRigState ?: TestingRigState(), succ.testingRigState ?: TestingRigState()))
                "slider" -> result.add(SliderTransition(env?.sliderState ?: SliderState(), succ.sliderState ?: SliderState()))

            }
          /*  if (succ.roboticArmState != null) {
                result.add(RoboticArmTransition(env.roboticArmState ?: RoboticArmState(), succ.roboticArmState))
            }
            if (succ.conveyorState != null) {
                result.add(ConveyorTransition(env.conveyorState ?: ConveyorState(), succ.conveyorState))
            }
            if (succ.testingRigState != null) {
                result.add(TestingRigTransition(env.testingRigState ?: TestingRigState(), succ.testingRigState))
            }
            if (succ.sliderState != null) {
                result.add(SliderTransition(env.sliderState ?: SliderState(), succ.sliderState))
            }*/
            return result
        } else {
            emptyList()
        }
    }

    /**
     * Return the defined successor state of the latest matching state, according to the state machine
     */
    fun reset(stateMachine:StateMachine): List<Transition> {
        val successor = stateMachine?.states?.first() as BasicState
        val env = (stateMachine?.states?.first() as BasicState?)?.environment
        targetStates[stateMachine.name] = successor
        return if (env != null) {
            when (stateMachine.name) {
                "roboticArm" -> listOf(RoboticArmTransition(RoboticArmState(), env.roboticArmState ?: RoboticArmState()))
                "conveyor" ->  listOf( ConveyorTransition(ConveyorState(), env.conveyorState ?: ConveyorState()))
                "testingRig" -> listOf(SliderTransition(SliderState(), env.sliderState ?: SliderState()))
                "slider" ->  listOf(TestingRigTransition(TestingRigState(), env.testingRigState ?: TestingRigState()))
                else -> emptyList()
            }
           /* listOf(
                    RoboticArmTransition(RoboticArmState(), env.roboticArmState ?: RoboticArmState()),
                    ConveyorTransition(ConveyorState(), env.conveyorState ?: ConveyorState()),
                    SliderTransition(SliderState(), env.sliderState ?: SliderState()),
                    TestingRigTransition(TestingRigState(), env.testingRigState ?: TestingRigState())
            )*/
        } else {
            emptyList()
        }
    }

    fun atEndState(stateMachine: StateMachine) = stateMachine?.isEndState(latestMatches[stateMachine.name]?.first ?: BasicState()) ?: true

    /**
     * Transform successor into 'goto' commands for the MQTT API
     */
    fun transform(transition: Transition): List<String> {
        return when (transition) {
            is RoboticArmTransition -> {
                val target = transition.targetState
                listOf(
                        "base-goto ${target.basePosition} ${transition.baseSpeed}",
                        "main-arm-goto ${target.mainArmPosition} ${transition.mainArmSpeed}",
                        "second-arm-goto ${target.secondArmPosition} ${transition.secondArmSpeed}",
                        "head-goto ${target.headPosition} 1.0",
                        "head-mount-goto ${target.headMountPosition} 1.0",
                        "gripper-goto ${target.gripperPosition} 1.0"
                )
            }
            is SliderTransition -> {
                listOf("slider-goto ${transition.targetState.sliderPosition}")
            }
            is ConveyorTransition -> {
                val t = transition.targetState
                listOf(
                        if (t.adjusterPosition != null) {
                            "adjuster-goto ${transition.targetState.adjusterPosition}"
                        } else {
                            ""
                        }).filter { !it.isEmpty() }
            }
            is TestingRigTransition -> {
                val t = transition.targetState
                listOf(
                        if (t.platformPosition != null) {
                            "platform-goto ${t.platformPosition}"
                        } else {
                            ""
                        },
                        if (t.heatplateTemperature != null) {
                            "platform-heatup ${t.heatplateTemperature}"
                        } else {
                            ""
                        }
                ).filter { !it.isEmpty() }
            }
            else -> emptyList()
        }
    }

    /**
     * Match the given state (current environment state indicated by sensor values)
     * against all states defined in this class and returns the first matched state.
     *
     *
     * @return the matching state or null, if no match was found
     */
    private fun matchState(env: Environment, sm: StateMachine): Pair<BasicState, Boolean>? {

        val all = sm?.all() ?: emptyList()

        val matches = all.filter {
            env.matches(it.environment) || (it.altEnvironment != null && env.matches(it.altEnvironment!!))
        }

        return if (matches.isNotEmpty()) {
            val match = matches.first()
            Pair(match, env.matches(match.environment))
        } else {
            null
        }
    }
}
