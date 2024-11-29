package org.pns.protection

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.pns.core.protection.ProtectionFramework
import org.pns.core.protection.environmental.EnvironmentalAnalyzer
import org.pns.node.data.SensorDatabase

class ProtectionActivity : AppCompatActivity() {
    private lateinit var database: SensorDatabase
    private lateinit var environmentalAnalyzer: EnvironmentalAnalyzer
    private lateinit var protectionFramework: ProtectionFramework

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        database = SensorDatabase.getDatabase(applicationContext)
        environmentalAnalyzer = EnvironmentalAnalyzer()
        protectionFramework = ProtectionFramework()

        lifecycleScope.launch {
            database.sensorDao().getLatestReadings()
        }
    }
}
