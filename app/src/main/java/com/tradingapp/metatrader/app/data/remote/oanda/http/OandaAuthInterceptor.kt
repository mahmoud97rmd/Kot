package com.tradingapp.metatrader.app.data.remote.oanda.http

import okhttp3.Interceptor
import okhttp3.Response

class OandaAuthInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()?.trim().orEmpty()
        val req = chain.request().newBuilder().apply {
            if (token.isNotBlank()) {
                addHeader("Authorization", "Bearer $token")
            }
            addHeader("Accept", "application/json")
        }.build()
        return chain.proceed(req)
    }
}
