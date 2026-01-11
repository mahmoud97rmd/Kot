package com.tradingapp.metatrader.app.core.candles

data class Candle(
    val timeSec: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double = 0.0
)
