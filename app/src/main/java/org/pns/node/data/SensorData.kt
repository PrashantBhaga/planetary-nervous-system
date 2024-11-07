package org.pns.node.data

import android.content.Context
import androidx.room.*

@Entity(tableName = "sensor_readings")
data class SensorReading(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sensorType: Int,
    val value: Float,
    val condition: String,
    val meaning: String,
    val planetaryImpact: String,
    val ecosystemRole: String,    // Added this field
    val recommendation: String,   // Added this field
    val nodeId: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface SensorReadingDao {
    @Insert
    suspend fun insert(reading: SensorReading)

    @Query("SELECT * FROM sensor_readings ORDER BY timestamp DESC")
    suspend fun getAllReadings(): List<SensorReading>
}

@Database(entities = [SensorReading::class], version = 2, exportSchema = false) // Incremented version
abstract class PlanetaryDatabase : RoomDatabase() {
    abstract fun sensorReadingDao(): SensorReadingDao

    companion object {
        @Volatile
        private var INSTANCE: PlanetaryDatabase? = null

        fun getDatabase(context: Context): PlanetaryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlanetaryDatabase::class.java,
                    "planetary_database"
                )
                    .fallbackToDestructiveMigration() // Added this to handle schema changes
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}