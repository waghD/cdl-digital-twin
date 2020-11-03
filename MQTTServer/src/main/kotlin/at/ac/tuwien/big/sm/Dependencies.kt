package at.ac.tuwien.big.sm

/**
 * This class models the dependencies of a state, i.e. BasicState.
 * Dependencies indicate precondtions defined for a state in order
 * to transition into this state
 */
data class Dependencies (
    var pickupDetected: Boolean? = null,
    var testRigDetected: Boolean? = null,
    var states: List<String>? = null
)