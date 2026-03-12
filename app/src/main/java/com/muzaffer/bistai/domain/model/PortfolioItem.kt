package com.muzaffer.bistai.domain.model

/**
 * Kullanıcının portföyündeki tek bir hisse pozisyonu.
 */
data class PortfolioItem(
    val symbol: String = "",
    val lotSize: Double = 0.0,
    val averageCost: Double = 0.0,
    val addedAt: Long = System.currentTimeMillis()
) {
    // Toplam maliyet
    val totalInvestment: Double
        get() = lotSize * averageCost
}
