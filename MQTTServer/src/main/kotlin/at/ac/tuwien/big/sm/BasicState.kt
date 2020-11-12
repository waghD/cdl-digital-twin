package at.ac.tuwien.big.sm

import at.ac.tuwien.big.entity.state.CameraState
import at.ac.tuwien.big.entity.state.Environment

/**
 * Basic state in a state machine. [altEnvironment] defines an optional second matching environment, which can be used to
 * define a condition for a choice state. [altEnvironment] is only considered, if the successor state is a [ChoiceState].
 */
data class BasicState(
        override var name: String = "Snapshot",
        override var type: String = "BasicState",

        var sensor: String? = null,
        var dependencies: Dependencies = Dependencies(),
        var environment: Environment = Environment(),
        var altEnvironment: Environment? = null
) : StateBase() {
    /**
     * Checks if the conditions of this state are satisfied as precondition
     * to execute transition with target as this state
     */
    fun isSatisfied(latestMatches: Map<String, Pair<BasicState, Boolean>>, cameraState: CameraState) : Boolean {
        val pickupDetected = this.dependencies.pickupDetected
        val testRigDetected = this.dependencies.testRigDetected
        val stateDependencies = this.dependencies.states

        /* Camera dependencies */
        if (pickupDetected != null && cameraState.pickupDetected != pickupDetected) return false
        if (testRigDetected != null && cameraState.testRigDetected != testRigDetected) return false

        /* State dependencies */
        if(stateDependencies != null && stateDependencies.isNotEmpty()) {
            val latestMatchesNames = ArrayList<String>()
            for((_,pair) in latestMatches) {
                latestMatchesNames.add(pair.first.name)
            }

            if (latestMatchesNames.intersect(stateDependencies).size < stateDependencies.size) return false
        }
        return true
    }
}