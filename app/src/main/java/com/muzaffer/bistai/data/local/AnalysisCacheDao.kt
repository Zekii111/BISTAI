package com.muzaffer.bistai.data.local

import androidx.room.*

@Dao
interface AnalysisCacheDao {

    @Query("SELECT * FROM analysis_cache WHERE symbol = :symbol")
    suspend fun getBySymbol(symbol: String): AnalysisCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AnalysisCacheEntity)

    @Query("DELETE FROM analysis_cache WHERE generatedAt < :cutoffMs")
    suspend fun deleteOlderThan(cutoffMs: Long)
}
