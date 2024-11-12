package org.pns.node

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.pns.node.analysis.EnvironmentalAnalyzer
import org.pns.node.data.PlanetaryDatabase
import org.pns.node.data.SensorReading
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), SensorEventListener {
    private val nodeId = UUID.randomUUID().toString().take(8)
    private val sensorValues = mutableMapOf<Int, Float>()
    private var lastUpdateTime = 0L
    private lateinit var environmentalAnalyzer: EnvironmentalAnalyzer

    private lateinit var sensorManager: SensorManager
    private lateinit var database: PlanetaryDatabase
    private lateinit var webView: WebView
    private lateinit var readingsText: TextView
    private lateinit var scrollView: ScrollView
    private lateinit var startButton: Button
    private lateinit var shareButton: Button
    private lateinit var earthPulseView: EarthPulseView

    inner class WebAppInterface {
        @JavascriptInterface
        fun getSensorData(): String {
            val patterns = environmentalAnalyzer.getActivePatterns()
            val patternsJson = patterns.joinToString(",") { it.toJson() }

            return """
                {
                    "light": ${sensorValues[Sensor.TYPE_LIGHT] ?: 0f},
                    "pressure": ${sensorValues[Sensor.TYPE_PRESSURE] ?: 0f},
                    "patterns": [$patternsJson]
                }
            """.trimIndent()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        environmentalAnalyzer = EnvironmentalAnalyzer()
        initializeViews()
        checkPermissions()
    }

    private fun initializeViews() {
        readingsText = findViewById(R.id.readingsText)
        scrollView = findViewById(R.id.readingsScrollView)
        startButton = findViewById(R.id.startButton)
        shareButton = findViewById(R.id.shareButton)
        earthPulseView = findViewById(R.id.earthPulseView)

        findViewById<TextView>(R.id.nodeIdText).text = "Earth Connection Point #$nodeId"

        webView = findViewById<WebView>(R.id.earthPulseWebView).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            addJavascriptInterface(WebAppInterface(), "Android")
            loadUrl("file:///android_asset/pulse.html")
        }

        startButton.setOnClickListener { startSensing() }
        shareButton.setOnClickListener { shareReadings() }
    }

    private fun startSensing() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        database = PlanetaryDatabase.getDatabase(applicationContext)

        var activeSensors = 0
        listOf(Sensor.TYPE_LIGHT, Sensor.TYPE_PRESSURE).forEach { type ->
            sensorManager.getDefaultSensor(type)?.let { sensor ->
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
                activeSensors++
            }
        }

        readingsText.text = """
            🌍 Earth Connection Established
            
            Monitoring through $activeSensors sensors...
            Analyzing ecosystem patterns...
            
        """.trimIndent()
    }

    override fun onSensorChanged(event: SensorEvent) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTime < 1000) return  // Update once per second
        lastUpdateTime = currentTime

        val type = event.sensor.type
        val value = event.values[0]
        sensorValues[type] = value

        // Add reading to analyzer and get updated patterns
        environmentalAnalyzer.addReading(type, value)
        val patterns = environmentalAnalyzer.getActivePatterns()

        // Update WebView
        webView.evaluateJavascript("""
            updateSensorData(${WebAppInterface().getSensorData()})
        """.trimIndent(), null)

        // Update database and UI with pattern information
        if (patterns.isNotEmpty()) {
            val currentPattern = patterns.first()

            lifecycleScope.launch {
                // Store reading with pattern information
                database.sensorReadingDao().insert(SensorReading(
                    timestamp = currentTime,
                    nodeId = nodeId,
                    sensorType = type,
                    value = value,
                    condition = currentPattern.type.toString(),
                    meaning = currentPattern.description,
                    planetaryImpact = currentPattern.ecosystemImpact,
                    ecosystemRole = currentPattern.speciesActivity.joinToString(", ") {
                        "${it.species}: ${it.activity}"
                    },
                    recommendation = currentPattern.recommendations.joinToString(", ")
                ))

                // Update UI
                val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                readingsText.append("""
                    ═══════════════════════
                    🕒 $timestamp
                    
                    📊 Pattern: ${currentPattern.type}
                    Confidence: ${(currentPattern.confidence * 100).toInt()}%
                    
                    🌍 ${currentPattern.description}
                    Impact: ${currentPattern.ecosystemImpact}
                    
                    🦋 Active Species:
                    ${currentPattern.speciesActivity.joinToString("\n") {
                    "• ${it.species} (${(it.likelihood * 100).toInt()}%): ${it.activity}"
                }}
                    
                    💡 Recommendations:
                    ${currentPattern.recommendations.joinToString("\n") { "• $it" }}
                    ═══════════════════════
                    
                """.trimIndent() + "\n\n")

                scrollView.fullScroll(View.FOCUS_DOWN)
            }
        }

        // Update EarthPulseView with sensor data
        earthPulseView.updatePulse(sensorValues[Sensor.TYPE_LIGHT] ?: 0f, sensorValues[Sensor.TYPE_PRESSURE] ?: 0f)
    }

    private fun shareReadings() = lifecycleScope.launch {
        try {
            val readings = database.sensorReadingDao().getAllReadings()
            val shareText = buildString {
                append("🌍 Earth Connection Report #$nodeId\n\n")
                readings.take(10).forEach { reading ->
                    append("═══════════════════════\n")
                    append("Pattern: ${reading.condition}\n")
                    append("Description: ${reading.meaning}\n")
                    append("Impact: ${reading.planetaryImpact}\n")
                    append("Species Activity: ${reading.ecosystemRole}\n")
                    append("Recommendations: ${reading.recommendation}\n")
                    append("═══════════════════════\n\n")
                }
                if (readings.size > 10) {
                    append("... and ${readings.size - 10} more observations")
                }
            }
            startActivity(android.content.Intent.createChooser(android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            }, "Share Earth's Messages"))
        } catch (e: Exception) {
            Log.e("MainActivity", "Share error: ${e.message}")
        }
    }

    private fun checkPermissions() {
        val permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissions.isEmpty()) startSensing()
        else ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 100)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startSensing()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
}