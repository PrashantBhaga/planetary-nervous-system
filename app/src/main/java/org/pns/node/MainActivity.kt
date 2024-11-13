package org.pns.node

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.pns.node.data.PlanetaryData
import org.pns.node.data.PlanetaryDatabase

class MainActivity : AppCompatActivity() {
    private lateinit var database: PlanetaryDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        database = PlanetaryDatabase.getDatabase(applicationContext)
        
        // Example data insertion
        lifecycleScope.launch {
            insertSampleData()
        }
    }

    private suspend fun insertSampleData() {
        val sampleData = PlanetaryData(
            nodeId = "node1",
            sensorType = "temperature",
            value = 25.5f,
            condition = "normal",
            meaning = "optimal temperature",
            planetaryImpact = "stable",
            ecosystemRole = "maintaining balance",
            recommendation = "continue monitoring",
            type = "environmental"
        )
        
        database.planetaryDao().insert(sampleData)
    }
}
