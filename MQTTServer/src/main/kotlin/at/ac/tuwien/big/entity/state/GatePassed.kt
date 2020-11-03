package at.ac.tuwien.big.entity.state

import at.ac.tuwien.big.sm.Dependencies

/**
 * Signals an object passing through a gate. Gates act as a light barrier.
 */
data class GatePassed(
        override var name: String = "Snapshot",
        override var entity: String = "Gate",
        override var dependencies: Dependencies = Dependencies(),

        var channel: String
) : StateEvent {
    override fun match(other: StateEvent, similar: (Double, Double) -> Boolean) = this == other
}