package com.muzaffer.bistai.data.di

import com.muzaffer.bistai.data.remote.StockApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /** Borsa API sağlayıcısı **/
    private const val BASE_URL = "https://api.bistai.com/v1/"

    /** Yahoo Finance API base URL'i **/
    private const val YF_BASE_URL = "https://query1.finance.yahoo.com/"

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            // API anahtarı header'ı — gerçek anahtarı BuildConfig.STOCK_API_KEY ile tanımla
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    // .addHeader("Authorization", "Bearer ${BuildConfig.STOCK_API_KEY}")
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideStockApiService(retrofit: Retrofit): StockApiService =
        retrofit.create(StockApiService::class.java)

    @Provides
    @Singleton
    fun provideYfApiService(okHttpClient: OkHttpClient): com.muzaffer.bistai.data.remote.YfApiService =
        Retrofit.Builder()
            .baseUrl(YF_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(com.muzaffer.bistai.data.remote.YfApiService::class.java)
}
