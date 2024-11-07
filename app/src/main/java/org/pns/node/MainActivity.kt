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
import org.pns.node.data.PlanetaryDatabase
import org.pns.node.data.SensorReading
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), SensorEventListener {
    private val nodeId = UUID.randomUUID().toString().take(8)
    private val sensorValues = mutableMapOf<Int, Float>()
    private var lastUpdateTime = 0L

    private lateinit var sensorManager: SensorManager
    private lateinit var database: PlanetaryDatabase
    private lateinit var webView: WebView
    private lateinit var readingsText: TextView
    private lateinit var scrollView: ScrollView
    private lateinit var startButton: Button
    private lateinit var shareButton: Button

    inner class WebAppInterface {
        @JavascriptInterface
        fun getSensorData() = """{"light":${sensorValues[Sensor.TYPE_LIGHT] ?: 0f},
            "pressure":${sensorValues[Sensor.TYPE_PRESSURE] ?: 0f}}"""
    }

    data class EarthPattern(
        val trend: String,
        val description: String,
        val prediction: String,
        val action: String
    )

    private val patterns = mapOf(
        Sensor.TYPE_LIGHT to { value: Float ->
            when {
                value < 50 -> EarthPattern(
                    "Night/Shade",
                    "Earth's regeneration period",
                    "Nocturnal ecosystem activity",
                    "Monitor night patterns"
                )
                value < 1000 -> EarthPattern(
                    "Transition",
                    "Dawn/Dusk period",
                    "Species transition time",
                    "Track behavioral changes"
                )
                else -> EarthPattern(
                    "Full Light",
                    "Peak activity period",
                    "Maximum ecosystem energy",
                    "Observe peak patterns"
                )
            }
        },
        Sensor.TYPE_PRESSURE to { value: Float ->
            when {
                value < 980 -> EarthPattern(
                    "Low Pressure",
                    "Weather system developing",
                    "Potential weather changes",
                    "Monitor system evolution"
                )
                value < 1020 -> EarthPattern(
                    "Normal Pressure",
                    "Stable conditions",
                    "Maintaining patterns",
                    "Record baseline data"
                )
                else -> EarthPattern(
                    "High Pressure",
                    "Clear weather system",
                    "Stable conditions ahead",
                    "Track stability effects"
                )
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeViews()
        checkPermissions()
    }

    private fun initializeViews() {
        readingsText = findViewById(R.id.readingsText)
        scrollView = findViewById(R.id.readingsScrollView)
        startButton = findViewById(R.id.startButton)
        shareButton = findViewById(R.id.shareButton)

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
        patterns.keys.forEach { type ->
            sensorManager.getDefaultSensor(type)?.let { sensor ->
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
                activeSensors++
            }
        }

        readingsText.text = "🌍 Earth Connection Established\n\nMonitoring through $activeSensors sensors...\n\n"
    }

    override fun onSensorChanged(event: SensorEvent) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTime < 1000) return  // Update once per second
        lastUpdateTime = currentTime

        val type = event.sensor.type
        val value = event.values[0]
        sensorValues[type] = value

        webView.evaluateJavascript("updateSensorData(${WebAppInterface().getSensorData()})", null)

        patterns[type]?.let { interpreter ->
            val pattern = interpreter(value)
            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

            lifecycleScope.launch {
                database.sensorReadingDao().insert(SensorReading(
                    timestamp = currentTime,
                    nodeId = nodeId,
                    sensorType = type,
                    value = value,
                    condition = pattern.trend,
                    meaning = pattern.description,
                    planetaryImpact = pattern.prediction,
                    ecosystemRole = pattern.action,
                    recommendation = ""
                ))

                readingsText.append("""
                    ═══════════════════════
                    🕒 $timestamp
                    
                    📊 ${pattern.trend}
                    Value: $value
                    
                    🌍 ${pattern.description}
                    🔮 ${pattern.prediction}
                    💡 ${pattern.action}
                    ═══════════════════════
                    
                """.trimIndent() + "\n\n")

                scrollView.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

    private fun shareReadings() = lifecycleScope.launch {
        try {
            val readings = database.sensorReadingDao().getAllReadings()
            val shareText = buildString {
                append("🌍 Earth Connection Report #$nodeId\n\n")
                readings.forEach { reading ->
                    append("═══════════════════════\n")
                    append("Status: ${reading.condition}\n")
                    append("Earth Speaks: ${reading.meaning}\n")
                    append("Prediction: ${reading.planetaryImpact}\n")
                    append("═══════════════════════\n\n")
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