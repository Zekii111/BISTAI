package com.muzaffer.bistai.data.local.fake

import com.muzaffer.bistai.domain.model.NewsItem
import com.muzaffer.bistai.domain.model.NewsSentiment
import javax.inject.Inject

/**
 * Gerçek bir News API olmadan haber verilerini simüle eden sahte kaynak.
 * Gerçek API entegrasyonunda yalnızca bu sınıf değişecek; Repository ve ViewModel
 * aynı kalacak (Clean Architecture gereği).
 */
class FakeNewsDataSource @Inject constructor() {

    /** [symbol] varlık için güncel 5 haber başlığı döner (simüle). */
    fun getNewsForAsset(symbol: String): List<NewsItem> {
        return when (symbol.uppercase()) {

            // ─── Altın ───────────────────────────────────────────────────
            "ALTIN", "GOLD", "XAUUSD" -> listOf(
                NewsItem("Fed faiz kararı öncesi altın talebi artıyor", "Bloomberg", NewsSentiment.POSITIVE),
                NewsItem("Orta Doğu gerginliği güvenli liman talebini yükseltiyor", "Reuters", NewsSentiment.POSITIVE),
                NewsItem("Dolar endeksi yükseldi, altın baskı altında", "CNBC", NewsSentiment.NEGATIVE),
                NewsItem("Merkez bankaları altın alımlarını sürdürüyor", "FT", NewsSentiment.POSITIVE),
                NewsItem("ETF çıkışları altın fiyatını baskılıyor", "WSJ", NewsSentiment.NEGATIVE)
            )

            // ─── Gümüş ───────────────────────────────────────────────────
            "GUMUS", "SILVER", "XAGUSD" -> listOf(
                NewsItem("Sanayi talebi gümüşü destekliyor", "Bloomberg", NewsSentiment.POSITIVE),
                NewsItem("Güneş paneli üretimi artışı gümüş ihtiyacını artırıyor", "Reuters", NewsSentiment.POSITIVE),
                NewsItem("Altın/gümüş oranı tarihi yüksekte", "FT", NewsSentiment.NEUTRAL),
                NewsItem("Tesla gümüş bağlantısızlık testleri devam ediyor", "CNBC", NewsSentiment.NEUTRAL),
                NewsItem("Yatırımcılar gümüşten altına geçiyor", "WSJ", NewsSentiment.NEGATIVE)
            )

            // ─── Bitcoin ─────────────────────────────────────────────────
            "BTCUSD", "BTC", "BITCOIN" -> listOf(
                NewsItem("BlackRock Bitcoin ETF'sine rekor giriş", "CoinDesk", NewsSentiment.POSITIVE),
                NewsItem("El Salvador Bitcoin rezervini artırıyor", "Bloomberg", NewsSentiment.POSITIVE),
                NewsItem("SEC yeni kripto düzenleme taslağını duyurdu", "Reuters", NewsSentiment.NEGATIVE),
                NewsItem("Halving sonrası madenci geliri düşüyor", "CoinTelegraph", NewsSentiment.NEGATIVE),
                NewsItem("Kurumsal BTC alımları rekor seviyeye ulaştı", "CNBC", NewsSentiment.POSITIVE)
            )

            // ─── Dolar/TL ─────────────────────────────────────────────────
            "USDTRY", "DOLAR", "USD" -> listOf(
                NewsItem("TCMB faiz kararı beklentileri değişiyor", "Hürriyet", NewsSentiment.NEGATIVE),
                NewsItem("Cari açık verileri TL üzerinde baskı oluşturdu", "Milliyet", NewsSentiment.NEGATIVE),
                NewsItem("Turizm gelirleri döviz rezervlerini destekliyor", "Bloomberg HT", NewsSentiment.POSITIVE),
                NewsItem("Fed güvercin mesaj verdi, dolar geriledi", "Reuters", NewsSentiment.POSITIVE),
                NewsItem("Yabancı yatırımcı TL varlıklarına ilgi artıyor", "Sabah", NewsSentiment.POSITIVE)
            )

            // ─── BIST Genel ───────────────────────────────────────────────
            "THYAO" -> listOf(
                NewsItem("THYAO yeni uçuş rotaları açıkladı, koltuk kapasitesi artıyor", "Bloomberg HT", NewsSentiment.POSITIVE),
                NewsItem("Jet yakıt fiyatları THY karlılığını baskılıyor", "Dünya", NewsSentiment.NEGATIVE),
                NewsItem("THY kargo gelirleri rekor seviyede", "NTV Para", NewsSentiment.POSITIVE),
                NewsItem("Dolar/TL kuru THY'nin döviz borcunu ağırlaştırıyor", "Milliyet", NewsSentiment.NEGATIVE),
                NewsItem("Turizm sezonu THY yolcu sayısını yukarı çekiyor", "Sabah", NewsSentiment.POSITIVE)
            )
            "GARAN" -> listOf(
                NewsItem("Garanti BBVA net karını %18 artırdı", "Bloomberg HT", NewsSentiment.POSITIVE),
                NewsItem("BDDK yeni sermaye yeterliliği kuralları bankacılığı etkiliyor", "Dünya", NewsSentiment.NEGATIVE),
                NewsItem("Faiz indirimi beklentisi banka marjlarını sıkıştırıyor", "Reuters", NewsSentiment.NEGATIVE),
                NewsItem("Garanti dijital bankacılıkta kullanıcı tabanını genişletti", "NTV Para", NewsSentiment.POSITIVE),
                NewsItem("Kredi büyümesi tahminlerin üzerinde gerçekleşti", "Milliyet", NewsSentiment.POSITIVE)
            )

            // ─── Varsayılan (diğer BIST hisseleri) ──────────────────────
            else -> listOf(
                NewsItem("$symbol hisseleri bu hafta sektör ortalamalarının üzerinde getiri sağladı", "Bloomberg HT", NewsSentiment.POSITIVE),
                NewsItem("Kurumsal yatırımcılar $symbol pozisyonlarını artırıyor", "NTV Para", NewsSentiment.POSITIVE),
                NewsItem("Yükselen hammadde fiyatları $symbol marjlarını baskılıyor", "Dünya", NewsSentiment.NEGATIVE),
                NewsItem("BIST 100 genel yükseliş trendi sürdürüyor", "Milliyet", NewsSentiment.POSITIVE),
                NewsItem("Döviz kuru belirsizliği yatırımcı iştahını kısıtlıyor", "Sabah", NewsSentiment.NEGATIVE)
            )
        }
    }

    /** Güncel makro ekonomik bağlam (simüle). */
    fun getMacroContext(): String = """
        TCMB Politika Faizi: %42,5 (son indirim: Şubat 2025)
        ABD Fed Faizi: %4,25-4,50 (yıl içinde 2 indirim beklentisi)
        Enflasyon (TÜFE): Yıllık %62,4 (düşüş trendinde)
        Dolar/TL: 36,20 (son 3 ayda %4 TL değer kaybı)
        BIST 100: 10.240 puan (YTD: %8 artış)
        VIX (Küresel Korku Endeksi): 18,5 (nötr bölge)
        Petrol (Brent): 82 USD/varil (jeopolitik risk primiyle)
        Altın: 2.040 USD/ons (Fed beklentileriyle pozitif korelasyon)
    """.trimIndent()
}
