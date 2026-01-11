package com.tradingapp.metatrader.app.features.oanda.net

object OandaEndpoints {
    // OANDA v20: pricing stream is an HTTP streaming endpoint. :contentReference[oaicite:3]{index=3}
    fun pricingStreamBase(env: String): String =
        if (env.equals("live", true)) "https://stream-fxtrade.oanda.com"
        else "https://stream-fxpractice.oanda.com"

    fun restBase(env: String): String =
        if (env.equals("live", true)) "https://api-fxtrade.oanda.com"
        else "https://api-fxpractice.oanda.com"
}
