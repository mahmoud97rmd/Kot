package com.tradingapp.metatrader.app.core.market

data class MarketTick(
    val instrument: String,
    val timeEpochMs: Long,
    val bid: Double,
    val ask: Double
)
