package com.tradingapp.metatrader.app.data.remote.oanda.http

import com.tradingapp.metatrader.app.core.oanda.OandaEnvironment

object OandaEndpoints {
    fun restBaseUrl(env: OandaEnvironment): String {
        return when (env) {
            OandaEnvironment.PRACTICE -> "https://api-fxpractice.oanda.com"
            OandaEnvironment.LIVE -> "https://api-fxtrade.oanda.com"
        }
    }

    fun streamBaseUrl(env: OandaEnvironment): String {
        return when (env) {
            OandaEnvironment.PRACTICE -> "https://stream-fxpractice.oanda.com"
            OandaEnvironment.LIVE -> "https://stream-fxtrade.oanda.com"
        }
    }
}
