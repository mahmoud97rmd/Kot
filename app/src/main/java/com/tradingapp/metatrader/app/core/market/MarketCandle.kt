package com.tradingapp.metatrader.app.core.market

data class MarketCandle(
    val timeSec: Long,   // candle open time in seconds
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long = 0L
)
