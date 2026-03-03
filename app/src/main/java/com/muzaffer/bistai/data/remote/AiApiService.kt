package com.muzaffer.bistai.data.remote

import android.util.Log
import com.muzaffer.bistai.BuildConfig
import com.muzaffer.bistai.domain.model.AiPrediction
import com.muzaffer.bistai.domain.model.NewsItem
import com.muzaffer.bistai.domain.model.Trend
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gemini REST API — v1beta endpoint + gemini-flash-latest model.
 *
 * Endpoint: https://generativelanguage.googleapis.com/v1beta/
 * Auth: X-goog-api-key header
 *
 * Medyum AI: Haber + makro bağlam + JSON çıktı formatı ile derinlemesine analiz.
 */
@Singleton
class AiApiService @Inject constructor() {

    companion object {
        private const val TAG      = "BISTAI_AI"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        private const val MODEL    = "gemini-flash-latest"   // ← senin curl komutundakiyle aynı
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(90, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val isApiKeySet: Boolean get() = BuildConfig.GEMINI_API_KEY.isNotBlank()

    // ─── Medyum AI Analizi ───────────────────────────────────────────────────

    /**
     * Varlık için Medyum AI analizi üretir.
     * Haber başlıkları + makro bağlam ile zenginleştirilmiş prompt.
     * Çıktı: Yapılandırılmış JSON → AiPrediction
     */
    suspend fun analyzeAsset(
        symbol: String,
        newsItems: List<NewsItem>,
        macroContext: String,
        currentPrice: Double = 0.0
    ): Result<AiPrediction> {
        if (!isApiKeySet) {
            Log.e(TAG, "❌ API Key boş!")
            return Result.failure(IllegalStateException("API_KEY_MISSING"))
        }

        val newsBlock = newsItems.mapIndexed { i, item ->
            "${i + 1}. [${item.sentiment.name}] ${item.headline} (${item.source})"
        }.joinToString("\n")

        val prompt = """
Sen BIST ve Küresel Piyasalar konusunda uzman bir "Medyum AI" analistisin.
Haberlerin fiyat üzerindeki psikolojik ve teknik etkisini analiz et.

VARLIK: $symbol${if (currentPrice > 0) " | Güncel Fiyat: $currentPrice" else ""}

SON 5 HABER BAŞLIĞI:
$newsBlock

MAKRO EKONOMİK DURUM:
$macroContext

GÖREV:
Bu verileri bir finansal medyum gibi analiz ederek YALNIZCA aşağıdaki JSON formatında yanıt ver:
{
  "trend": "BULLISH",
  "confidence": 72,
  "targetLow": 290.0,
  "targetHigh": 320.0,
  "reasoning": "Analizin özeti burada (2-3 cümle, Türkçe)"
}

KURALLAR:
- trend: sadece BULLISH, BEARISH veya NEUTRAL olabilir
- confidence: 0-100 arası sayı (kaç % emin olduğun)
- targetLow/targetHigh: gerçekçi fiyat aralığı (0.0 ise bilinmiyor)
- reasoning: Türkçe, 2-3 cümle, haberlerin fiyata etkisini açıkla
- SADECE JSON döndür, başka hiçbir şey yazma
        """.trimIndent()

        return try {
            val result = generateContent(listOf(userMessage(prompt)))
            result.map { rawText -> parseAiPrediction(rawText) }
        } catch (e: Exception) {
            Log.e(TAG, "💥 analyzeAsset hatası: ${e.message}", e)
            Result.failure(e)
        }
    }

    /** Geriye dönük uyumluluk için eski hisse analizi (sadece metin). */
    suspend fun analyzeStock(symbol: String): Result<String> {
        if (!isApiKeySet) return Result.failure(IllegalStateException("API_KEY_MISSING"))
        val prompt = """
            BIST hisselerini analiz eden bir finans uzmanısın.
            "$symbol" hissesi için Türkçe, kısa (max 250 kelime) analiz yaz:
            📊 Teknik Görünüm | 📈 Yatırımcı Özeti | ⚠️ Dikkat Edilmesi Gerekenler
        """.trimIndent()
        return generateContent(listOf(userMessage(prompt)))
    }

    // ─── Sohbet Yönetimi ─────────────────────────────────────────────────────

    fun startStockChat(symbol: String, prediction: AiPrediction? = null): SimpleChat {
        val predContext = prediction?.let {
            """
            Not: Bu hisse için AI tahmini: ${it.trend.label} (%${it.confidenceScore} güven)
            Hedef fiyat: ${it.targetLow} – ${it.targetHigh}
            """.trimIndent()
        } ?: ""

        val systemPrompt = """
            Sen BISTAI adlı bir finans asistanısın. "$symbol" hissesi için yapılmış 
            bir AI analizinin detaylarını kullanıcıyla tartışıyorsun.
            $predContext
            - Türkçe yanıt ver, kısa ve odaklı ol (max 200 kelime).
            - Sadece bu analiz ve "$symbol" hakkında konuş.
            - Yatırım tavsiyesi verme, analiz ve bilgi sun.
        """.trimIndent()

        return SimpleChat(
            initialHistory = mutableListOf(
                userMessage(systemPrompt),
                modelMessage("Analiz hazır! **$symbol** ile ilgili her sorunuzu yanıtlamaya hazırım.")
            )
        )
    }

    fun startGeneralChat(): SimpleChat {
        val systemPrompt = """
            Sen BISTAI adlı Türk finans ve borsa asistanısın.
            BIST ve global piyasalar hakkında Türkçe, kısa yanıtlar veriyorsun.
            Yatırım tavsiyesi yerine analiz ve bilgi sunuyorsun.
        """.trimIndent()
        return SimpleChat(
            initialHistory = mutableListOf(
                userMessage(systemPrompt),
                modelMessage("Merhaba! Ben **BISTAI**, kişisel borsa danışmanın. Sormak istediğin her şeyi yanıtlamaya hazırım!")
            )
        )
    }

    suspend fun sendMessage(chat: SimpleChat, message: String): Result<String> {
        chat.addUserMessage(message)
        val result = generateContent(chat.history)
        result.onSuccess { reply -> chat.addModelMessage(reply) }
        return result
    }

    // ─── REST Core ───────────────────────────────────────────────────────────

    private suspend fun generateContent(contents: List<JSONObject>): Result<String> {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val url = "$BASE_URL/$MODEL:generateContent"
        val body = JSONObject().apply {
            put("contents", JSONArray(contents))
            put("generationConfig", JSONObject().apply {
                put("maxOutputTokens", 2048)
                put("temperature", 0.7)
            })
        }

        Log.d(TAG, "📤 URL: $url")
        Log.d(TAG, "📤 Body uzunluğu: ${body.toString().length} karakter")

        return try {
            val request = Request.Builder()
                .url(url)
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .addHeader("X-goog-api-key", apiKey)   // ← Kullanıcının curl'ü ile aynı
                .build()

            val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                client.newCall(request).execute()
            }

            val responseBody = response.body?.string() ?: ""
            Log.d(TAG, "📥 HTTP ${response.code}")
            Log.d(TAG, "📥 Yanıt: $responseBody")

            if (!response.isSuccessful) {
                Log.e(TAG, "❌ Hata! Kod=${response.code} | $responseBody")
                return Result.failure(Exception(parseApiError(response.code, responseBody)))
            }

            val json = JSONObject(responseBody)
            val text = json
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")

            Log.d(TAG, "✅ Başarılı (${text.length} karakter)")
            Result.success(text)
        } catch (e: Exception) {
            Log.e(TAG, "💥 İstisna: ${e.javaClass.simpleName}: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ─── AI Yanıt Parser ─────────────────────────────────────────────────────

    private fun parseAiPrediction(rawText: String): AiPrediction {
        return try {
            // JSON bloğunu ayıkla (Gemini bazen ``` ile sarar)
            val jsonStr = rawText
                .replace("```json", "")
                .replace("```", "")
                .trim()
            val json = JSONObject(jsonStr)

            val trend = when (json.optString("trend", "NEUTRAL").uppercase()) {
                "BULLISH" -> Trend.BULLISH
                "BEARISH" -> Trend.BEARISH
                else      -> Trend.NEUTRAL
            }

            AiPrediction(
                trend           = trend,
                confidenceScore = json.optInt("confidence", 50).coerceIn(0, 100),
                targetLow       = json.optDouble("targetLow", 0.0),
                targetHigh      = json.optDouble("targetHigh", 0.0),
                reasoning       = json.optString("reasoning", "Analiz tamamlandı."),
                rawAnalysis     = rawText
            )
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ JSON parse başarısız, ham metin kullanılıyor: ${e.message}")
            AiPrediction(
                trend           = Trend.NEUTRAL,
                confidenceScore = 50,
                targetLow       = 0.0,
                targetHigh      = 0.0,
                reasoning       = rawText.take(300),
                rawAnalysis     = rawText
            )
        }
    }

    private fun parseApiError(code: Int, body: String): String = when (code) {
        400  -> "Geçersiz istek. API anahtarınızı kontrol edin."
        401, 403 -> "API_KEY_INVALID: Anahtar yetkisiz. aistudio.google.com'dan yeni key alın."
        404  -> "API_MODEL_NOT_FOUND: '$MODEL' modeli bulunamadı."
        429  -> "QUOTA_EXCEEDED: Kota aşıldı. Birkaç dakika bekleyin."
        else -> "API Hatası ($code)"
    }

    // ─── Yardımcı ────────────────────────────────────────────────────────────

    private fun userMessage(text: String) = JSONObject().apply {
        put("role", "user"); put("parts", JSONArray().put(JSONObject().put("text", text)))
    }
    private fun modelMessage(text: String) = JSONObject().apply {
        put("role", "model"); put("parts", JSONArray().put(JSONObject().put("text", text)))
    }
}

/** SDK bağımsız sohbet geçmişi yöneticisi. */
class SimpleChat(val initialHistory: MutableList<JSONObject> = mutableListOf()) {
    val history: List<JSONObject> get() = initialHistory.toList()
    fun addUserMessage(t: String) { initialHistory.add(u(t)) }
    fun addModelMessage(t: String) { initialHistory.add(m(t)) }
    private fun u(t: String) = JSONObject().apply { put("role","user");  put("parts", JSONArray().put(JSONObject().put("text",t))) }
    private fun m(t: String) = JSONObject().apply { put("role","model"); put("parts", JSONArray().put(JSONObject().put("text",t))) }
}
