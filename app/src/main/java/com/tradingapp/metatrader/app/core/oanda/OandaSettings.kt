package com.tradingapp.metatrader.app.core.oanda

data class OandaSettings(
    val apiToken: String,
    val accountId: String,
    val environment: OandaEnvironment
)

enum class OandaEnvironment {
    PRACTICE,
    LIVE
}
