package com.muzaffer.bistai.data.repository

import com.muzaffer.bistai.data.local.fake.FakeNewsDataSource
import com.muzaffer.bistai.domain.model.NewsItem
import com.muzaffer.bistai.domain.repository.NewsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepositoryImpl @Inject constructor(
    private val fakeNewsDataSource: FakeNewsDataSource
) : NewsRepository {

    override fun getNewsForAsset(symbol: String): List<NewsItem> =
        fakeNewsDataSource.getNewsForAsset(symbol)

    override fun getMacroContext(): String =
        fakeNewsDataSource.getMacroContext()
}
