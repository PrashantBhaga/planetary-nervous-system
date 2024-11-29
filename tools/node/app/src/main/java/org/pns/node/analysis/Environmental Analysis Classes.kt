package org.pns.node.analysis

import android.hardware.Sensor
import kotlin.math.abs

class EnvironmentalAnalyzer(
    private val historyWindow: Int = 10
) {
    private val lightReadings = ArrayDeque<Float>(historyWindow)
    private val pressureReadings = ArrayDeque<Float>(historyWindow)
    private val patterns = mutableListOf<EnvironmentalPattern>()

    fun addReading(type: Int, value: Float) {
        when (type) {
            Sensor.TYPE_LIGHT -> {
                if (lightReadings.size >= historyWindow) lightReadings.removeFirst()
                lightReadings.addLast(value)
            }
            Sensor.TYPE_PRESSURE -> {
                if (pressureReadings.size >= historyWindow) pressureReadings.removeFirst()
                pressureReadings.addLast(value)
            }
        }
        analyzePatterns()
    }

    private fun analyzePatterns() {
        patterns.clear()

        // Analyze light transitions
        val lightTrend = calculateTrend(lightReadings)
        when {
            isDawnTransition() -> patterns.add(createDawnChorusPattern())
            isNocturnalPeriod() -> patterns.add(createNocturnalPattern())
            isPeakDaylight() -> patterns.add(createPhotosynthesisPattern())
        }

        // Analyze pressure changes
        val pressureTrend = calculateTrend(pressureReadings)
        when {
            isStormApproaching() -> patterns.add(createStormPattern())
            isClearWeather() -> patterns.add(createClearWeatherPattern())
        }

        // Analyze combined patterns
        if (isMigrationWeather()) {
            patterns.add(createMigrationPattern())
        }

        if (isEcosystemStress()) {
            patterns.add(createStressPattern())
        }
    }

    private fun calculateTrend(readings: List<Float>): Float {
        if (readings.size < 2) return 0f
        return (readings.last() - readings.first()) / readings.size
    }

    private fun isDawnTransition(): Boolean {
        if (lightReadings.size < 3) return false
        val trend = calculateTrend(lightReadings)
        return trend > 0 && lightReadings.last() in 10f..200f
    }

    private fun isNocturnalPeriod(): Boolean {
        return lightReadings.isNotEmpty() && lightReadings.last() < 10f
    }

    private fun isPeakDaylight(): Boolean {
        return lightReadings.isNotEmpty() && lightReadings.last() > 10000f
    }

    private fun isStormApproaching(): Boolean {
        if (pressureReadings.size < 3) return false
        val trend = calculateTrend(pressureReadings)
        return trend < -0.5f && pressureReadings.last() < STORM_PRESSURE_THRESHOLD
    }

    private fun isClearWeather(): Boolean {
        return pressureReadings.isNotEmpty() && pressureReadings.last() > CLEAR_WEATHER_THRESHOLD
    }

    private fun isMigrationWeather(): Boolean {
        return isClearWeather() && isDaytime() && !isEcosystemStress()
    }

    private fun isEcosystemStress(): Boolean {
        return isExtremeLight() || isExtremePressure()
    }

    private fun isDaytime(): Boolean {
        return lightReadings.isNotEmpty() && lightReadings.last() > DAWN_LIGHT_THRESHOLD
    }

    private fun isExtremeLight(): Boolean {
        return lightReadings.isNotEmpty() && lightReadings.last() > EXTREME_LIGHT_THRESHOLD
    }

    private fun isExtremePressure(): Boolean {
        return pressureReadings.isNotEmpty() &&
                (pressureReadings.last() < EXTREME_LOW_PRESSURE ||
                        pressureReadings.last() > EXTREME_HIGH_PRESSURE)
    }

    private fun createDawnChorusPattern() = EnvironmentalPattern(
        type = PatternType.DAWN_CHORUS,
        confidence = 0.85f,
        description = "Dawn Chorus Period - Transition from night to day",
        ecosystemImpact = "Peak activity period for many bird species and early pollinators",
        speciesActivity = listOf(
            SpeciesActivity("Songbirds", "Morning chorus and territory marking", 0.9f),
            SpeciesActivity("Bees", "Beginning foraging activities", 0.8f),
            SpeciesActivity("Nocturnal Mammals", "Returning to shelter", 0.7f)
        ),
        recommendations = listOf(
            "Ideal time for bird watching and nature observation",
            "Monitor pollinator activity as they begin daily cycles",
            "Track the timing of dawn chorus across seasons"
        )
    )

    private fun createNocturnalPattern() = EnvironmentalPattern(
        type = PatternType.NOCTURNAL_ACTIVITY,
        confidence = 0.9f,
        description = "Nocturnal Activity Period",
        ecosystemImpact = "Active period for nocturnal species and night-blooming plants",
        speciesActivity = listOf(
            SpeciesActivity("Owls", "Active hunting", 0.85f),
            SpeciesActivity("Moths", "Pollination activities", 0.9f),
            SpeciesActivity("Night-blooming Plants", "Flower opening", 0.95f)
        ),
        recommendations = listOf(
            "Observe nocturnal pollinator activity",
            "Monitor night-blooming flowers",
            "Track owl and bat activity patterns"
        )
    )

    private fun createPhotosynthesisPattern() = EnvironmentalPattern(
        type = PatternType.PHOTOSYNTHESIS_PEAK,
        confidence = 0.95f,
        description = "Peak Photosynthesis Period",
        ecosystemImpact = "Maximum energy capture by plants",
        speciesActivity = listOf(
            SpeciesActivity("Plants", "Peak photosynthesis", 0.95f),
            SpeciesActivity("Pollinators", "Maximum foraging", 0.9f),
            SpeciesActivity("Birds", "Active feeding", 0.85f)
        ),
        recommendations = listOf(
            "Monitor plant growth rates",
            "Track pollinator activity levels",
            "Observe solar energy utilization"
        )
    )

    private fun createStormPattern() = EnvironmentalPattern(
        type = PatternType.STORM_APPROACHING,
        confidence = 0.8f,
        description = "Storm System Approaching",
        ecosystemImpact = "Changing weather conditions affecting animal behavior",
        speciesActivity = listOf(
            SpeciesActivity("Birds", "Seeking shelter", 0.9f),
            SpeciesActivity("Insects", "Reduced activity", 0.85f),
            SpeciesActivity("Plants", "Preparing for rainfall", 0.7f)
        ),
        recommendations = listOf(
            "Monitor animal shelter-seeking behavior",
            "Track pressure change effects",
            "Prepare for rainfall impact"
        )
    )

    private fun createClearWeatherPattern() = EnvironmentalPattern(
        type = PatternType.CLEAR_WEATHER,
        confidence = 0.9f,
        description = "Clear Weather System",
        ecosystemImpact = "Stable conditions supporting regular activity patterns",
        speciesActivity = listOf(
            SpeciesActivity("Birds", "Normal foraging", 0.9f),
            SpeciesActivity("Butterflies", "Active flight", 0.95f),
            SpeciesActivity("Plants", "Regular growth", 0.9f)
        ),
        recommendations = listOf(
            "Monitor baseline activity patterns",
            "Track species diversity",
            "Observe normal behaviors"
        )
    )

    private fun createMigrationPattern() = EnvironmentalPattern(
        type = PatternType.MIGRATION_WEATHER,
        confidence = 0.7f,
        description = "Favorable Migration Conditions",
        ecosystemImpact = "Supporting bird and insect migration",
        speciesActivity = listOf(
            SpeciesActivity("Migratory Birds", "Active migration", 0.8f),
            SpeciesActivity("Butterflies", "Migration movement", 0.75f),
            SpeciesActivity("Local Birds", "Increased activity", 0.7f)
        ),
        recommendations = listOf(
            "Track migratory species movement",
            "Monitor flight patterns",
            "Observe seasonal transitions"
        )
    )

    private fun createStressPattern() = EnvironmentalPattern(
        type = PatternType.ECOSYSTEM_STRESS,
        confidence = 0.75f,
        description = "Ecosystem Stress Conditions",
        ecosystemImpact = "Environmental conditions causing adaptation needs",
        speciesActivity = listOf(
            SpeciesActivity("Plants", "Stress responses", 0.8f),
            SpeciesActivity("Insects", "Behavioral changes", 0.7f),
            SpeciesActivity("Birds", "Modified patterns", 0.6f)
        ),
        recommendations = listOf(
            "Monitor stress indicators",
            "Track adaptation behaviors",
            "Observe recovery patterns"
        )
    )

    fun getActivePatterns(): List<EnvironmentalPattern> = patterns

    companion object {
        private const val STORM_PRESSURE_THRESHOLD = 980f
        private const val CLEAR_WEATHER_THRESHOLD = 1020f
        private const val DAWN_LIGHT_THRESHOLD = 100f
        private const val PEAK_LIGHT_THRESHOLD = 10000f
        private const val EXTREME_LIGHT_THRESHOLD = 50000f
        private const val EXTREME_LOW_PRESSURE = 950f
        private const val EXTREME_HIGH_PRESSURE = 1040f
    }
}