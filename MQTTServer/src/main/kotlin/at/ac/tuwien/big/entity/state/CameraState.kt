package at.ac.tuwien.big.entity.state

import at.ac.tuwien.big.sm.Dependencies

/**
 * Only used in StateObserver - not part of the environment!
 * Use is to hold the current detection states of work piece on conveyor belt (ready to be picked up)
 * and on test rig.
 */

data class CameraState(
        override var name: String = "Snapshot",
        override var entity: String = "Camera",
        override var dependencies: Dependencies = Dependencies(),

        var pickupDetected: Boolean? = null,
        var testRigDetected: Boolean? =null
) : StateEvent {
     override fun match(other: StateEvent, similar: (Double, Double) -> Boolean): Boolean {
        return if (other is CameraState) {
            return this.pickupDetected == other.pickupDetected && this.testRigDetected == other.testRigDetected
        } else {
            false
        }
    }
}