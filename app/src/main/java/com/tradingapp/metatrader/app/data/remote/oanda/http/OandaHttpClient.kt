package com.tradingapp.metatrader.app.data.remote.oanda.http

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object OandaHttpClient {

    fun create(tokenProvider: () -> String?): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BASIC

        return OkHttpClient.Builder()
            .addInterceptor(OandaAuthInterceptor(tokenProvider))
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS) // IMPORTANT for streaming (no timeout)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }
}
