package at.ac.tuwien.big.entity.state

import at.ac.tuwien.big.sm.Dependencies

/**
 * State of the conveyor
 */
data class ConveyorState(
        override var name: String = "Snapshot",
        override var entity: String = "Conveyor",
        override var dependencies: Dependencies = Dependencies(),

        val adjusterPosition: Double? = null
) : StateEvent {
    override fun match(other: StateEvent, similar: (Double, Double) -> Boolean): Boolean {
        return if (other is ConveyorState) {
            val adjusterSimilar = if (this.adjusterPosition != null && other.adjusterPosition != null) {
                similar(this.adjusterPosition, other.adjusterPosition)
            } else {
                true
            }
            return adjusterSimilar
        } else {
            false
        }
    }
}
