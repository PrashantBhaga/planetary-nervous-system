package org.pns.node.data

import androidx.room.*

@Entity(tableName = "planetary_data")
data class PlanetaryData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nodeId: String,
    val sensorType: String,
    val value: Float,
    val condition: String,
    val meaning: String,
    val planetaryImpact: String,
    val ecosystemRole: String,
    val recommendation: String,
    val type: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface PlanetaryDao {
    @Insert
    suspend fun insert(data: PlanetaryData)

    @Query("SELECT * FROM planetary_data ORDER BY timestamp DESC")
    suspend fun getAllData(): List<PlanetaryData>
}

@Database(entities = [PlanetaryData::class], version = 1)
abstract class PlanetaryDatabase : RoomDatabase() {
    abstract fun planetaryDao(): PlanetaryDao

    companion object {
        @Volatile
        private var INSTANCE: PlanetaryDatabase? = null

        fun getDatabase(context: android.content.Context): PlanetaryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlanetaryDatabase::class.java,
                    "planetary_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
