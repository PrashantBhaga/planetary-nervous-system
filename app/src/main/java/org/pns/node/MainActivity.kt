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
    private lateinit var sensorManager: SensorManager
    private lateinit var database: PlanetaryDatabase
    private lateinit var webView: WebView

    // Fixed views property with explicit types
    private val views: Map<String, View> by lazy {
        mapOf(
            "readings" to findViewById<TextView>(R.id.readingsText),
            "nodeId" to findViewById<TextView>(R.id.nodeIdText),
            "scroll" to findViewById<ScrollView>(R.id.readingsScrollView),
            "start" to findViewById<Button>(R.id.startButton),
            "share" to findViewById<Button>(R.id.shareButton)
        )
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun getSensorData() = """{"light":${sensorValues[Sensor.TYPE_LIGHT] ?: 0f},
            "pressure":${sensorValues[Sensor.TYPE_PRESSURE] ?: 0f}}"""
    }

    data class PlanetaryMeaning(
        val condition: String,
        val meaning: String,
        val planetaryImpact: String,
        val ecosystemRole: String,
        val recommendation: String
    )

    private val planetarySensors = mapOf(
        Sensor.TYPE_LIGHT to { value: Float ->
            when {
                value < 10 -> PlanetaryMeaning(
                    condition = "Night Time / Deep Shade",
                    meaning = "Earth's regeneration period",
                    planetaryImpact = "Supporting nocturnal biodiversity",
                    ecosystemRole = "Enabling nocturnal species activities",
                    recommendation = "Monitor ecosystem regeneration"
                )
                value < 200 -> PlanetaryMeaning(
                    condition = "Dawn/Dusk Transition",
                    meaning = "Critical ecosystem transition",
                    planetaryImpact = "Circadian rhythm sync",
                    ecosystemRole = "Species behavioral changes",
                    recommendation = "Monitor activity transitions"
                )
                else -> PlanetaryMeaning(
                    condition = "Daylight",
                    meaning = "Peak photosynthetic period",
                    planetaryImpact = "Maximum productivity",
                    ecosystemRole = "Energy flow activation",
                    recommendation = "Track energy patterns"
                )
            }
        },
        Sensor.TYPE_PRESSURE to { value: Float ->
            when {
                value < 980 -> PlanetaryMeaning(
                    condition = "Low Pressure System",
                    meaning = "Active dynamics",
                    planetaryImpact = "Weather development",
                    ecosystemRole = "Species behavior shift",
                    recommendation = "Monitor weather changes"
                )
                value < 1020 -> PlanetaryMeaning(
                    condition = "Normal Pressure",
                    meaning = "Stable conditions",
                    planetaryImpact = "Regular patterns",
                    ecosystemRole = "Normal functions",
                    recommendation = "Observe baseline"
                )
                else -> PlanetaryMeaning(
                    condition = "High Pressure",
                    meaning = "Clear conditions",
                    planetaryImpact = "Weather stability",
                    ecosystemRole = "Clear-weather activity",
                    recommendation = "Monitor behavior"
                )
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupViews()
        checkPermissions()
    }

    private fun setupViews() {
        (views["nodeId"] as? TextView)?.text = "Earth Connection Point #$nodeId"
        webView = findViewById<WebView>(R.id.earthPulseWebView).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            addJavascriptInterface(WebAppInterface(), "Android")
            loadUrl("file:///android_asset/pulse.html")
        }
        views["start"]?.setOnClickListener { startSensing() }
        views["share"]?.setOnClickListener { shareReadings() }
    }

    private fun startSensing() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        database = PlanetaryDatabase.getDatabase(applicationContext)

        var activeSensors = 0
        planetarySensors.keys.forEach { type ->
            sensorManager.getDefaultSensor(type)?.let { sensor ->
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
                activeSensors++
            }
        }

        (views["readings"] as? TextView)?.apply {
            text = "🌍 Earth Connection Established\n\nListening through $activeSensors sensors...\n\n"
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        val type = event.sensor.type
        val value = event.values[0]
        sensorValues[type] = value

        webView.evaluateJavascript("updateSensorData(${WebAppInterface().getSensorData()})", null)

        planetarySensors[type]?.let { interpreter ->
            val reading = interpreter(value)
            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

            lifecycleScope.launch {
                database.sensorReadingDao().insert(SensorReading(
                    timestamp = System.currentTimeMillis(),
                    nodeId = nodeId,
                    sensorType = type,
                    value = value,
                    condition = reading.condition,
                    meaning = reading.meaning,
                    planetaryImpact = reading.planetaryImpact,
                    ecosystemRole = reading.ecosystemRole,
                    recommendation = reading.recommendation
                ))

                (views["readings"] as? TextView)?.append("""
                    ═══════════════════════
                    🕒 $timestamp
                    Value: $value
                    
                    🌍 ${reading.condition}
                    🌱 ${reading.meaning}
                    🌐 ${reading.planetaryImpact}
                    🍃 ${reading.ecosystemRole}
                    💡 ${reading.recommendation}
                    ═══════════════════════
                    
                """.trimIndent() + "\n\n")

                (views["scroll"] as? ScrollView)?.fullScroll(View.FOCUS_DOWN)
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
                    append("Condition: ${reading.condition}\n")
                    append("Message: ${reading.meaning}\n")
                    append("Impact: ${reading.planetaryImpact}\n")
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