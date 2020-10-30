package at.ac.tuwien.big.sm

/**
 * A job defines a sequence of states that should be executed to complete it.
 */
data class Job(
        val id: String = "",
        var name: String = "",
        val target: String = "",
        val roboticArmStates: MutableList<StateBase> = mutableListOf(),
        val conveyorStates: MutableList<StateBase> = mutableListOf(),
        val sliderStates: MutableList<StateBase> = mutableListOf(),
        val testingRigStates: MutableList<StateBase> = mutableListOf()
)
