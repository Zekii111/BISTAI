package com.muzaffer.bistai.domain.model

/**
 * Gemini AI'ın bir varlık (hisse/emtia/kripto/döviz) için ürettiği tahmin.
 */
data class AiPrediction(
    val trend: Trend,
    val confidenceScore: Int,   // 0–100
    val targetLow: Double,
    val targetHigh: Double,
    val reasoning: String,
    val rawAnalysis: String
) {
    companion object {
        val EMPTY = AiPrediction(Trend.NEUTRAL, 0, 0.0, 0.0, "", "")
    }
}

enum class Trend(val label: String, val emoji: String) {
    BULLISH("Yükseliş Beklentisi", "🐂"),
    BEARISH("Düşüş Beklentisi",    "🐻"),
    NEUTRAL("Yatay Seyir",         "⚖️")
}
