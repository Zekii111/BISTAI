package com.muzaffer.bistai.domain.repository

import com.muzaffer.bistai.domain.model.AiPrediction
import com.muzaffer.bistai.domain.model.NewsItem

enum class AnalysisSource { FIREBASE, LOCAL_CACHE, FRESH_API }

data class AnalysisResult(
    val prediction: AiPrediction,
    val source: AnalysisSource,
    val generatedAt: Long               // epoch ms — kaç dakika önce üretildi
)

/**
 * Analiz havuzu soyutlaması.
 * Uygulama: Firebase → Gemini API → Room cache öncelik sırası.
 */
interface AnalysisRepository {
    suspend fun getAnalysis(
        symbol: String,
        newsItems: List<NewsItem>,
        macroContext: String
    ): Result<AnalysisResult>
}

