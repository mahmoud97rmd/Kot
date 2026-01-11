package com.tradingapp.metatrader.app.features.backtest.data.oanda

import okhttp3.Interceptor
import okhttp3.Response

class OandaAuthInterceptor(
    private val tokenProvider: () -> String
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${tokenProvider()}")
            .build()
        return chain.proceed(req)
    }
}
