package at.ac.tuwien.big

import at.ac.tuwien.big.entity.state.RoboticArmState
import at.ac.tuwien.big.entity.transition.ConveyorTransition
import at.ac.tuwien.big.entity.transition.RoboticArmTransition
import at.ac.tuwien.big.entity.transition.TestingRigTransition
import at.ac.tuwien.big.entity.transition.Transition
import org.influxdb.InfluxDB
import org.influxdb.InfluxDB.ConsistencyLevel
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.BatchPoints
import org.influxdb.dto.Point
import java.util.concurrent.TimeUnit

/**
 * Store data points in a time series database
 */
class TimeSeriesDatabase(host: String) {

    private val dbName = "pick-and-place"
    private var influxDB: InfluxDB = InfluxDBFactory.connect("http://$host:8086", "root", "root")

    init {
        influxDB.createDatabase(dbName)
        influxDB.enableBatch(100, 1000, TimeUnit.MILLISECONDS)
    }

    /**
     * Reset the database
     */
    fun resetDatabase() {
        influxDB.deleteDatabase(dbName)
        influxDB.createDatabase(dbName)
    }

    /**
     * Save a robotic arm measurement point together with a reference point.
     */
    fun savePoint(state: RoboticArmState, label: String? = null) {
        val batchPoints = BatchPoints
                .database(dbName)
                .consistency(ConsistencyLevel.ALL)
                .build()
        val pointBuilder = Point.measurement("robotic_arm")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("base", state.basePosition)
                .addField("main_arm", state.mainArmPosition)
                .addField("second_arm", state.secondArmPosition)
                .addField("headMount", state.headMountPosition)
                .addField("gripper", state.gripperPosition)
        val point = if (label != null) {
            pointBuilder.addField("label", label).build()
        } else {
            pointBuilder.build()
        }
        batchPoints.point(point)
        influxDB.write(batchPoints)
    }

    /**
     * Save the measurement event of a state transition
     */
    fun savePoint(transition: Transition) {
        val batchPoints = BatchPoints
                .database(dbName)
                .consistency(ConsistencyLevel.ALL)
                .build()
        val tag = when (transition) {
            is RoboticArmTransition -> "robotic_arm"
            is ConveyorTransition -> "conveyor"
            is TestingRigTransition -> "testing_rig"
            else -> ""
        }
        val point = Point.measurement("successor")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("unit", tag)
                .addField("state", transition.targetState.name)
                .addField("progress", 1)
                .build()
        batchPoints.point(point)
        influxDB.write(batchPoints)
    }

    /**
     * Log a successful production cycle, i.e. from picking up the item to dropping it
     */
    fun logSuccessfulProduction() {
        val batchPoints = BatchPoints
                .database(dbName)
                .consistency(ConsistencyLevel.ALL)
                .build()
        val point = Point.measurement("productions")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("count", 1)
                .build()
        batchPoints.point(point)
        influxDB.write(batchPoints)
    }
}
