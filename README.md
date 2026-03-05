# 🤖 BISTAI — Yapay Zeka Destekli Borsa İstanbul Terminali

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white"/>
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white"/>
  <img src="https://img.shields.io/badge/AI-Gemini%20Flash-4285F4?style=for-the-badge&logo=google&logoColor=white"/>
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white"/>
  <img src="https://img.shields.io/badge/Min%20SDK-26-brightgreen?style=for-the-badge"/>
</p>

<p align="center">
  <b>BIST hisseleri, emtialar, kripto ve döviz için gerçek zamanlı fiyat takibi ve Gemini AI destekli analiz uygulaması.</b>
</p>

---

## ✨ Özellikler

| Özellik | Açıklama |
|---|---|
| 📈 **Canlı Fiyatlar** | Yahoo Finance API ile BIST hisseleri, altın, gümüş, BTC ve USD/TL |
| 🤖 **Medyum AI** | Google Gemini Flash ile haber + makro bağlam destekli tahmin analizi |
| 📰 **Türkçe Haberler** | Google News RSS ile sınırsız, ücretsiz BIST haberleri |
| 💬 **AI Sohbet** | Her hisse için yapay zeka ile interaktif analiz tartışması |
| ⭐ **İzleme Listesi** | Favori hisseleri Room veritabanında sakla |
| 🌙 **Karanlık / Aydınlık Tema** | Material 3 ile tam tema desteği |
| 📊 **Duygu Analizi** | Haber başlıklarına göre POSITIVE / NEGATIVE / NEUTRAL sentiment |

---

## 📱 Desteklenen Varlıklar

**BIST Hisseleri:** THYAO, GARAN, SISE, YKBNK, KCHOL, EREGL, AKBNK, ASELS, BIMAS, TUPRS, PGSUS, TOASO, FROTO, SAHOL, TAVHL

**Emtialar:** Altın (TL/gram), Gümüş (TL/gram)

**Kripto & Döviz:** Bitcoin (BTC/USD), Dolar (USD/TRY)

---

## 🏗️ Teknoloji Stack

```
📦 BISTAI
├── 🎨 UI              → Jetpack Compose + Material 3
├── 🧠 AI              → Google Gemini Flash (REST API)
├── 📈 Hisse Fiyatları → Yahoo Finance API (ücretsiz, key gerektirmez)
├── 📰 Haberler        → Google News RSS (ücretsiz, sınırsız)
├── 💾 Veritabanı      → Room (izleme listesi)
├── 💉 DI              → Dagger Hilt
├── 🌐 Network         → Retrofit 2 + OkHttp 4
└── ⚡ Async           → Kotlin Coroutines
```

---

## 🚀 Kurulum

### Gereksinimler
- Android Studio Hedgehog veya üstü
- Android SDK 26+
- Gemini API Key ([aistudio.google.com](https://aistudio.google.com/app/apikey) üzerinden ücretsiz alın)

### Adımlar

```bash
# 1. Repoyu klonla
git clone https://github.com/mlio-n/BISTAI.git
cd BISTAI

# 2. API anahtarını tanımla
echo "GEMINI_API_KEY=senin_api_anahtarin" >> local.properties

# 3. Android Studio'da aç ve çalıştır
```

> **Not:** `local.properties` dosyası `.gitignore`'a eklidir, API anahtarın güvende kalır.

---

## ⚙️ API Yapısı

| API | Kullanım | Limit | Key |
|---|---|---|---|
| **Yahoo Finance** | Hisse & emtia fiyatları | ✅ Sınırsız | ❌ Yok |
| **Google News RSS** | Türkçe haberler | ✅ Sınırsız | ❌ Yok |
| **Google Gemini Flash** | AI analiz & sohbet | 1.500 istek/gün (ücretsiz) | ✅ Gerekli |

---

## 🗂️ Proje Yapısı

```
app/src/main/java/com/muzaffer/bistai/
├── data/
│   ├── di/              # Hilt modülleri (NetworkModule, DatabaseModule)
│   ├── local/           # Room veritabanı, DAO, fake data
│   ├── remote/          # API servisleri (Yahoo Finance, Google News RSS, Gemini)
│   ├── repository/      # Repository implementasyonları
│   └── mapper/          # DTO → Domain dönüşümleri
├── domain/
│   ├── model/           # Domain modelleri (Stock, NewsItem, AiPrediction)
│   ├── repository/      # Repository arayüzleri
│   └── util/            # Resource wrapper
├── presentation/        # ViewModel'lar
└── ui/                  # Compose ekranları & tema
```

---

## 🤖 AI Analiz Nasıl Çalışır?

```
1. Hisse seçilir (örn. THYAO)
        ↓
2. Yahoo Finance'den güncel fiyat çekilir
        ↓
3. Google News RSS'den son 5 Türkçe haber çekilir
        ↓
4. Haberler sentiment analizine tabi tutulur (POSITIVE/NEGATIVE/NEUTRAL)
        ↓
5. Gemini Flash'a prompt gönderilir:
   "THYAO | Fiyat: 285 TL | Son haberler: ..."
        ↓
6. Gemini JSON formatında döner:
   { trend: BULLISH, confidence: 72, targetLow: 290, targetHigh: 320 }
        ↓
7. Kullanıcıya gösterilir 🎯
```

---

## 📄 Lisans

Bu proje [MIT Lisansı](LICENSE) ile lisanslanmıştır.

---

<p align="center">
  <i>⭐ Beğendiysen yıldız atmayı unutma!</i>
</p>
