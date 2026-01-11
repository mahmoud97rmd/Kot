package com.tradingapp.metatrader.app.core.trading.mt5sim

data class PriceQuote(
    val timeSec: Long,
    val bid: Double,
    val ask: Double
) {
    val mid: Double get() = (bid + ask) / 2.0
}
