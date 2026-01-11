package com.tradingapp.metatrader.app.di

object AppConfig {
    // ضع بياناتك هنا (لاحقًا ننقلها إلى DataStore + NDK إن أردت)
    const val OANDA_ACCOUNT_ID = "REPLACE_ME"
    const val OANDA_TOKEN = "REPLACE_ME"
    const val OANDA_PRACTICE = true

    const val OANDA_REST_BASE_URL_PRACTICE = "https://api-fxpractice.oanda.com/"
    const val OANDA_REST_BASE_URL_LIVE = "https://api-fxtrade.oanda.com/"
}
