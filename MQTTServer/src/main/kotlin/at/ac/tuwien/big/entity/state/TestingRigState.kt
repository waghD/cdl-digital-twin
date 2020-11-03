package at.ac.tuwien.big.entity.state

import at.ac.tuwien.big.sm.Dependencies

/**
 * State of the testing rig
 */
data class TestingRigState(
        override var name: String = "Snapshot",
        override var entity: String = "TestingRig",
        override var dependencies: Dependencies = Dependencies(),

        val objectCategory: ObjectCategory? = null,
        val platformPosition: Double? = null,
        val heatplateTemperature: Double? = null
) : StateEvent {

    override fun match(other: StateEvent, similar: (Double, Double) -> Boolean): Boolean {
        return if (other is TestingRigState) {
            val platform = if (this.platformPosition != null && other.platformPosition != null) {
                similar(this.platformPosition, other.platformPosition)
            } else {
                true
            }
            val heatplate = if (this.heatplateTemperature != null && other.heatplateTemperature != null) {
                similar(this.heatplateTemperature, other.heatplateTemperature)
            } else {
                !(this.heatplateTemperature == null && other.heatplateTemperature != null)
            }
            val objectCategory = (other.objectCategory == null) || (this.objectCategory == other.objectCategory)
            objectCategory && platform && heatplate
        } else {
            false
        }
    }
}
