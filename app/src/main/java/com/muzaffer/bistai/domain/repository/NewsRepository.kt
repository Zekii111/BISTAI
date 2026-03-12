package com.muzaffer.bistai.domain.repository

import com.muzaffer.bistai.domain.model.NewsItem

/**
 * Haber kaynağı soyutlaması.
 * Şu an FakeNewsDataSource kullanıyor — ileride gerçek News API'ye geçilecek.
 */
interface NewsRepository {
    suspend fun getNewsForAsset(symbol: String): List<NewsItem>
    suspend fun getMacroContext(): String
}
