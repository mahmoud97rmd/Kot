package com.tradingapp.metatrader.app.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tradingapp.metatrader.data.remote.api.OandaApiService
import com.tradingapp.metatrader.data.remote.interceptors.AuthInterceptor
import com.tradingapp.metatrader.data.remote.stream.OandaPricingStreamClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    @Provides @Singleton
    fun provideOkHttp(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { AppConfig.OANDA_TOKEN })
            .addInterceptor(logging)
            .build()
    }

    @Provides @Singleton
    fun provideRetrofit(okHttp: OkHttpClient, moshi: Moshi): Retrofit {
        val baseUrl = if (AppConfig.OANDA_PRACTICE) AppConfig.OANDA_REST_BASE_URL_PRACTICE else AppConfig.OANDA_REST_BASE_URL_LIVE
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides @Singleton
    fun provideOandaApi(retrofit: Retrofit): OandaApiService =
        retrofit.create(OandaApiService::class.java)

    @Provides @Singleton
    fun providePricingStreamClient(moshi: Moshi): OandaPricingStreamClient =
        OandaPricingStreamClient(
            accountId = AppConfig.OANDA_ACCOUNT_ID,
            tokenProvider = { AppConfig.OANDA_TOKEN },
            isPractice = AppConfig.OANDA_PRACTICE,
            moshi = moshi
        )
}
