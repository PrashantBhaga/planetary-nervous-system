package org.pns.node.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "sensor_readings")
data class SensorReading(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val value: Float,
    val timestamp: Long
)

@Dao
interface SensorDao {
    @Insert
    suspend fun insert(reading: SensorReading)

    @Query("SELECT * FROM sensor_readings ORDER BY timestamp DESC LIMIT 1")
    fun getLatestReadings(): Flow<List<SensorReading>>
}

@Database(entities = [SensorReading::class], version = 1)
abstract class SensorDatabase : RoomDatabase() {
    abstract fun sensorDao(): SensorDao

    companion object {
        @Volatile
        private var INSTANCE: SensorDatabase? = null

        fun getDatabase(context: android.content.Context): SensorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SensorDatabase::class.java,
                    "sensor_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
