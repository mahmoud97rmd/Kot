package com.tradingapp.metatrader.domain.models

import java.time.Instant

data class Candle(
    val time: Instant,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long = 0L
)
