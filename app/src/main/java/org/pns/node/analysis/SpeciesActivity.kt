package org.pns.node.analysis

data class SpeciesActivity(
    val species: String,
    val activity: String,
    val likelihood: Float
) {
    fun toJson(): String {
        return """
            {
                "species": "$species",
                "activity": "$activity",
                "likelihood": $likelihood
            }
        """.trimIndent()
    }
}