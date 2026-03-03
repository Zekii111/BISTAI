package com.muzaffer.bistai.data.remote

import android.util.Log
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
 * Gemini REST v1 API — OkHttp üzerinden doğrudan çağrı.
 *
 * SDK'nın v1beta zorunluluğunu bypass eder.
 * Tüm istekler ve hatalar Logcat'e detaylı olarak yazdırılır.
 */
@Singleton
class AiApiService @Inject constructor() {

    companion object {
        private const val TAG     = "BISTAI_AI"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1/models"
        private const val MODEL    = "gemini-1.5-flash"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val isApiKeySet: Boolean get() = BuildConfig.GEMINI_API_KEY.isNotBlank()

    // ─── Tek Seferlik Analiz ─────────────────────────────────────────────────

    /** [symbol] hissesi için kısa teknik analiz üretir. */
    suspend fun analyzeStock(symbol: String): Result<String> {
        if (!isApiKeySet) {
            Log.e(TAG, "❌ API Key boş! BuildConfig.GEMINI_API_KEY tanımlı değil.")
            return Result.failure(IllegalStateException("API_KEY_MISSING"))
        }

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

    suspend fun sendMessage(chat: SimpleChat, message: String): Result<String> {
        chat.addUserMessage(message)
        val result = generateContent(chat.history)
        result.onSuccess { reply -> chat.addModelMessage(reply) }
        return result
    }

    // ─── REST Core (Detaylı Loglama ile) ────────────────────────────────────

    private suspend fun generateContent(contents: List<JSONObject>): Result<String> {
        val url = "$BASE_URL/$MODEL:generateContent?key=${BuildConfig.GEMINI_API_KEY}"
        val body = JSONObject().apply {
            put("contents", JSONArray(contents))
            put("generationConfig", JSONObject().apply {
                put("maxOutputTokens", 1024)
                put("temperature", 0.7)
            })
        }

        Log.d(TAG, "📤 İstek URL: $BASE_URL/$MODEL:generateContent?key=***")
        Log.d(TAG, "📤 İstek Body (character count): ${body.toString().length}")

        return try {
            val request = Request.Builder()
                .url(url)
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .build()

            val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                client.newCall(request).execute()
            }

            val responseBody = response.body?.string() ?: ""
            Log.d(TAG, "📥 HTTP Kodu: ${response.code}")
            Log.d(TAG, "📥 Ham Yanıt: $responseBody")

            if (!response.isSuccessful) {
                Log.e(TAG, "❌ API Hatası! Kod=${response.code} | Yanıt=$responseBody")
                val errorMsg = parseApiError(response.code, responseBody)
                return Result.failure(Exception(errorMsg))
            }

            val json = JSONObject(responseBody)
            val text = json
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")

            Log.d(TAG, "✅ Başarılı yanıt (${text.length} karakter)")
            Result.success(text)
        } catch (e: Exception) {
            Log.e(TAG, "💥 İstisna: ${e.javaClass.simpleName}: ${e.message}", e)
            Result.failure(e)
        }
    }

    /** HTTP hata koduna ve yanıt gövdesine göre kullanıcı dostu mesaj üretir. */
    private fun parseApiError(code: Int, body: String): String {
        return when (code) {
            400 -> "API İsteği Geçersiz: Lütfen API anahtarınızın doğru formatta olduğunu kontrol edin."
            401, 403 -> "API_KEY_INVALID: API anahtarınız geçersiz veya yetkisiz. Google AI Studio'dan " +
                    "(aistudio.google.com) Gemini 1.5 Flash için yetkilendirilmiş yeni bir anahtar alın."
            404 -> "API_MODEL_NOT_FOUND: '$MODEL' modeli bulunamadı. " +
                    "API anahtarınızın bu modele erişim yetkisi olmayabilir. " +
                    "aistudio.google.com adresinde yeni bir API key oluşturun."
            429 -> "QUOTA_EXCEEDED: Kota aşıldı. Lütfen birkaç dakika bekleyip tekrar deneyin."
            500, 503 -> "SUNUCU_HATASI: Google API geçici olarak kullanılamıyor. Daha sonra tekrar deneyin."
            else -> "Bilinmeyen Hata ($code): $body"
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

/** Sohbet geçmişi yöneticisi (SDK bağımlılığı olmadan). */
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
