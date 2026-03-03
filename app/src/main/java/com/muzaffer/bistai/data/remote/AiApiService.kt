package com.muzaffer.bistai.data.remote

import com.muzaffer.bistai.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gemini REST API'ye doğrudan OkHttp ile bağlanan servis.
 *
 * SDK'nın v1beta zorunluluğunu bypass ederek
 * https://generativelanguage.googleapis.com/v1/ endpoint'ini kullanır.
 * Bu sayede gemini-1.5-flash modeli sorunsuz çalışır.
 */
@Singleton
class AiApiService @Inject constructor() {

    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1/models"
        private const val MODEL    = "gemini-1.5-flash"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val isApiKeySet: Boolean get() = BuildConfig.GEMINI_API_KEY.isNotBlank()

    // ─── Tek Seferlik Analiz ─────────────────────────────────────────────────

    /** [symbol] hissesi için kısa teknik analiz ve yatırımcı özeti üretir. */
    suspend fun analyzeStock(symbol: String): Result<String> {
        if (!isApiKeySet) return Result.failure(IllegalStateException("API_KEY_MISSING"))

        val prompt = """
            BIST (Borsa İstanbul) hisselerini analiz eden bir finans uzmanısın.
            
            "$symbol" hissesi için aşağıdaki başlıkları içeren, Türkçe ve kısa (max 250 kelime) bir analiz yaz:
            
            📊 Teknik Görünüm: Kısa vadeli trend, destek/direnç seviyeleri.
            📈 Yatırımcı Özeti: Hem bireysel hem kurumsal yatırımcılar için fırsat/risk değerlendirmesi.
            ⚠️ Dikkat Edilmesi Gerekenler: Önemli riskler veya katalizörler.
            
            Yanıtını net, profesyonel ve aksiyon odaklı yaz. Kesin fiyat tahmini verme.
        """.trimIndent()

        return generateContent(listOf(userMessage(prompt)))
    }

    // ─── Sohbet Yönetimi ─────────────────────────────────────────────────────

    /**
     * [symbol] bağlamıyla başlayan bir sohbet oturumu döner.
     * İlk sistem mesajı geçmişe eklenir.
     */
    fun startStockChat(symbol: String): SimpleChat {
        val systemPrompt = """
            Sen BISTAI adlı bir finans asistanısın. Kullanıcı seninle "$symbol"
            (BIST - Borsa İstanbul) hissesi hakkında sohbet edecek.
            - Türkçe yanıt ver.
            - Finans terminolojisini sade ve anlaşılır kullan.
            - Yatırım tavsiyesi vermekten kaçın, analiz ve bilgi sun.
            - Kısa, odaklı ve net cevaplar ver (max 200 kelime).
        """.trimIndent()
        return SimpleChat(
            initialHistory = mutableListOf(
                userMessage(systemPrompt),
                modelMessage("Merhaba! Ben BISTAI finans asistanınım. **$symbol** hissesi hakkında her sorunuzu yanıtlamaya hazırım.")
            )
        )
    }

    /**
     * Genel borsa danışmanı sohbeti başlatır (hisse bağımsız).
     */
    fun startGeneralChat(): SimpleChat {
        val systemPrompt = """
            Sen BISTAI adlı bir Türk finans ve borsa asistanısın.
            - BIST (Borsa İstanbul) ve genel yatırım konularında uzmanısın.
            - Türkçe, sade ve anlaşılır yanıtlar veriyorsun.
            - Yatırım tavsiyesi yerine analiz ve bilgi sunuyorsun.
            - Kısa ve odaklı cevaplar veriyorsun (max 200 kelime).
        """.trimIndent()
        return SimpleChat(
            initialHistory = mutableListOf(
                userMessage(systemPrompt),
                modelMessage("Merhaba! Ben **BISTAI**, senin kişisel borsa danışmanın. BIST hisseleri, piyasa analizi veya yatırım stratejileri hakkında her soruyu yanıtlamaya hazırım!")
            )
        )
    }

    /** Mevcut sohbete mesaj gönderir ve yanıtı döner. */
    suspend fun sendMessage(chat: SimpleChat, message: String): Result<String> {
        chat.addUserMessage(message)
        val result = generateContent(chat.history)
        result.onSuccess { reply -> chat.addModelMessage(reply) }
        return result
    }

    // ─── REST Core ───────────────────────────────────────────────────────────

    private suspend fun generateContent(contents: List<JSONObject>): Result<String> {
        return try {
            val body = JSONObject().apply {
                put("contents", JSONArray(contents))
                put("generationConfig", JSONObject().apply {
                    put("maxOutputTokens", 1024)
                    put("temperature", 0.7)
                })
            }
            val url = "$BASE_URL/$MODEL:generateContent?key=${BuildConfig.GEMINI_API_KEY}"
            val request = Request.Builder()
                .url(url)
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                client.newCall(request).execute()
            }
            val responseBody = response.body?.string() ?: return Result.failure(Exception("Boş yanıt"))

            if (!response.isSuccessful) {
                return Result.failure(Exception("API Hatası (${response.code}): $responseBody"))
            }

            val json = JSONObject(responseBody)
            val text = json
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")

            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Yardımcı Fonksiyonlar ───────────────────────────────────────────────

    private fun userMessage(text: String) = JSONObject().apply {
        put("role", "user")
        put("parts", JSONArray().put(JSONObject().put("text", text)))
    }

    private fun modelMessage(text: String) = JSONObject().apply {
        put("role", "model")
        put("parts", JSONArray().put(JSONObject().put("text", text)))
    }
}

/**
 * SDK'nın Chat sınıfının yerini alan basit sohbet geçmişi yöneticisi.
 */
class SimpleChat(val initialHistory: MutableList<JSONObject> = mutableListOf()) {
    val history: List<JSONObject> get() = initialHistory.toList()
    fun addUserMessage(text: String) { initialHistory.add(userMsg(text)) }
    fun addModelMessage(text: String) { initialHistory.add(modelMsg(text)) }

    private fun userMsg(t: String) = JSONObject().apply {
        put("role", "user"); put("parts", JSONArray().put(JSONObject().put("text", t)))
    }
    private fun modelMsg(t: String) = JSONObject().apply {
        put("role", "model"); put("parts", JSONArray().put(JSONObject().put("text", t)))
    }
}
