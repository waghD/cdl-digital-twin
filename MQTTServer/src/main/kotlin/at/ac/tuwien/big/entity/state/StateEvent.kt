package at.ac.tuwien.big.entity.state

import at.ac.tuwien.big.sm.Dependencies

/**
 * Encapsulates sensor signals of one entity at one point in time
 */
interface StateEvent {
    var name: String
    var entity: String
    var dependencies: Dependencies

    fun match(other: StateEvent, similar: (Double, Double) -> Boolean): Boolean
}
