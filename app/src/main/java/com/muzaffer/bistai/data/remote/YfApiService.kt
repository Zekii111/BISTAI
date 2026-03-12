package com.muzaffer.bistai.data.remote

import com.muzaffer.bistai.data.remote.dto.yf.YfResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface YfApiService {
    
    /**
     * Yahoo Finance API endpoint'inden hisse detaylarını getirir.
     * @param symbols Virgülle ayrılmış hisse sembolleri listesi (örn. "THYAO.IS,GARAN.IS")
     * @param region Opsiyonel bölge parametresi (genelde "US" kullanılır)
     * @param lang Opsiyonel dil parametresi (genelde "en" kullanılır)
     */
    @GET("v7/finance/quote")
    suspend fun getQuotes(
        @Query("symbols") symbols: String,
        @Query("region") region: String = "US",
        @Query("lang") lang: String = "en"
    ): YfResponse
}
