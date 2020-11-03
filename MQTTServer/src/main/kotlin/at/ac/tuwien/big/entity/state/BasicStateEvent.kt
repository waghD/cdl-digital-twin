package at.ac.tuwien.big.entity.state

import at.ac.tuwien.big.sm.Dependencies

/**
 * Most basic [StateEvent]
 */
data class BasicStateEvent(
        override var name: String = "Basic",
        override var entity: String = "Empty",
        override var dependencies: Dependencies = Dependencies()
) : StateEvent {
    override fun match(other: StateEvent, similar: (Double, Double) -> Boolean) = this == other
}
