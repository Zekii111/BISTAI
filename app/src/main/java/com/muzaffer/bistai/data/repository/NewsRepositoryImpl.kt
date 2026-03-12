package com.muzaffer.bistai.data.repository

import com.muzaffer.bistai.data.remote.RemoteNewsDataSource
import com.muzaffer.bistai.domain.model.NewsItem
import com.muzaffer.bistai.domain.repository.NewsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepositoryImpl @Inject constructor(
    private val remoteNewsDataSource: RemoteNewsDataSource
) : NewsRepository {

    override suspend fun getNewsForAsset(symbol: String): List<NewsItem> =
        remoteNewsDataSource.getNewsForAsset(symbol)

    override suspend fun getMacroContext(): String =
        remoteNewsDataSource.getMacroContext()
}
