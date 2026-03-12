package com.muzaffer.bistai.data.remote

import com.muzaffer.bistai.data.remote.dto.StockDto
import javax.inject.Inject

class RemoteStockDataSource @Inject constructor(
    private val api: YfApiService
) {
    /**
     * BIST'teki hisselerin genel bir listesini (portföy izleme ekranı için) çeker.
     * Bu sabit sembolleri daha sonra veritabanı kontrollü de yapabiliriz.
     */
    suspend fun getStocks(): List<StockDto> {
        val symbolsList = listOf(
            "THYAO.IS", "GARAN.IS", "SISE.IS", "YKBNK.IS", "KCHOL.IS",
            "EREGL.IS", "AKBNK.IS", "ASELS.IS", "BIMAS.IS", "TUPRS.IS"
        )
        val symbolsStr = symbolsList.joinToString(",")
        
        return fetchFromYf(symbolsStr)
    }

    suspend fun getStockDetail(symbol: String): StockDto? {
        val yfSymbol = if (symbol.endsWith(".IS")) symbol else "$symbol.IS"
        return fetchFromYf(yfSymbol).firstOrNull()
    }

    suspend fun getBatchStocks(symbols: List<String>): List<StockDto> {
        val yfSymbols = symbols.map { if (it.endsWith(".IS")) it else "$it.IS" }.joinToString(",")
        return fetchFromYf(yfSymbols)
    }

    private suspend fun fetchFromYf(symbols: String): List<StockDto> {
        val response = api.getQuotes(symbols)
        return response.quoteResponse.result.map { yfQuote ->
            // Sembolün sonundaki ".IS" ekini kaldırarak (THYAO.IS -> THYAO) UI'da temiz gösterelim
            val cleanSymbol = yfQuote.symbol.replace(".IS", "")
            
            StockDto(
                symbol = cleanSymbol,
                name = yfQuote.longName ?: yfQuote.shortName ?: cleanSymbol,
                currentPrice = yfQuote.regularMarketPrice ?: 0.0,
                changePercent = yfQuote.regularMarketChangePercent ?: 0.0,
                previousClose = yfQuote.regularMarketPreviousClose,
                marketCap = yfQuote.marketCap,
                volume = yfQuote.regularMarketVolume
            )
        }
    }
}
