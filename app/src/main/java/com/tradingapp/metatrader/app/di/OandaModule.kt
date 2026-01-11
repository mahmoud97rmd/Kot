package com.tradingapp.metatrader.app.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tradingapp.metatrader.app.features.backtest.data.oanda.OandaApiService
import com.tradingapp.metatrader.app.features.backtest.data.oanda.OandaAuthInterceptor
import com.tradingapp.metatrader.app.features.backtest.data.oanda.OandaConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OandaModule {

    @Provides
    @Singleton
    fun provideOandaConfig(): OandaConfig {
        // TODO: ضع توكن demo هنا مؤقتاً، ثم ننقله لاحقاً إلى DataStore/NDK
        val token = "PUT_YOUR_OANDA_TOKEN_HERE"
        return OandaConfig(token = token, isPractice = true)
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Provides
    @Singleton
    fun provideOandaOkHttp(cfg: OandaConfig): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(OandaAuthInterceptor { cfg.token })
            .build()
    }

    @Provides
    @Singleton
    fun provideOandaRetrofit(cfg: OandaConfig, moshi: Moshi, ok: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(cfg.baseUrl)
            .client(ok)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideOandaApi(retrofit: Retrofit): OandaApiService =
        retrofit.create(OandaApiService::class.java)
}
