package com.muzaffer.bistai.domain.repository

import com.muzaffer.bistai.domain.model.PortfolioItem
import kotlinx.coroutines.flow.Flow

interface PortfolioRepository {
    /**
     * Kullanıcının portföyündeki tüm hisseleri stream olarak dinler.
     */
    fun getPortfolio(uid: String): Flow<List<PortfolioItem>>
    
    /**
     * Portföye yeni bir hisse ekler veya mevcut hissenin miktar/maliyetini günceller.
     */
    suspend fun addOrUpdatePortfolioItem(uid: String, item: PortfolioItem): Result<Unit>
    
    /**
     * Hissesi portföyden çıkarır.
     */
    suspend fun removePortfolioItem(uid: String, symbol: String): Result<Unit>
}
