package org.pns.node.analysis

data class EnvironmentalPattern(
    val type: PatternType,
    val confidence: Float,
    val description: String,
    val ecosystemImpact: String,
    val speciesActivity: List<SpeciesActivity>,
    val recommendations: List<String>
) {
    fun toJson(): String {
        return """
            {
                "type": "$type",
                "confidence": $confidence,
                "description": "$description",
                "ecosystemImpact": "$ecosystemImpact",
                "speciesActivity": [
                    ${speciesActivity.joinToString(",") { it.toJson() }}
                ],
                "recommendations": [
                    ${recommendations.joinToString(",") { "\"$it\"" }}
                ]
            }
        """.trimIndent()
    }
}