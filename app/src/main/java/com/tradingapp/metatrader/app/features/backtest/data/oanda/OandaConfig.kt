package com.tradingapp.metatrader.app.features.backtest.data.oanda

data class OandaConfig(
    val token: String,
    val isPractice: Boolean = true
) {
    val baseUrl: String
        get() = if (isPractice) "https://api-fxpractice.oanda.com/" else "https://api-fxtrade.oanda.com/"
}
