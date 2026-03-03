package com.muzaffer.bistai.domain.model

/** Bir varlık için elde edilen tek haber öğesi. */
data class NewsItem(
    val headline: String,
    val source: String,
    val sentiment: NewsSentiment
)

enum class NewsSentiment { POSITIVE, NEGATIVE, NEUTRAL }
