package org.pns.node

object PNSVersionInfo {
    const val MAJOR_VERSION = 0
    const val MINOR_VERSION = 2
    const val PATCH_VERSION = 0

    enum class Phase {
        BASIC_SENSING,
        EARTH_CONNECTION,
        FOREST_NETWORK,
        HEALING_PROTOCOLS
    }

    data class FeatureState(
        val implemented: Boolean = false,
        val version: String = "",
        val notes: String = ""
    )

    val features = mapOf(
        "basic_sensors" to FeatureState(
            implemented = true,
            version = "0.1.0",
            notes = "Initial sensor setup with basic readings"
        ),
        "planetary_interpretation" to FeatureState(
            implemented = true,
            version = "0.2.0",
            notes = "Enhanced readings with Earth connection metaphors"
        ),
        "visualization" to FeatureState(
            implemented = false,
            version = "",
            notes = "Pending implementation"
        ),
        "node_communication" to FeatureState(
            implemented = false,
            version = "",
            notes = "Planned for Forest Network phase"
        ),
        "healing_protocols" to FeatureState(
            implemented = false,
            version = "",
            notes = "Final phase implementation"
        )
    )

    fun getCurrentVersion() = "$MAJOR_VERSION.$MINOR_VERSION.$PATCH_VERSION"

    fun getFeatureStatus(): String {
        return buildString {
            appendLine("PNS Node Version ${getCurrentVersion()}")
            appendLine("Implementation Status:")
            features.forEach { (feature, state) ->
                appendLine("- $feature: ${if (state.implemented) "✓" else "⧖"} ${state.version}")
                if (state.notes.isNotEmpty()) {
                    appendLine("  Notes: ${state.notes}")
                }
            }
        }
    }
}