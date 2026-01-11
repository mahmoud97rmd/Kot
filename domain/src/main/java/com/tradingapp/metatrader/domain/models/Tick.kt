package com.tradingapp.metatrader.domain.models

import java.time.Instant

data class Tick(
    val instrument: String,
    val time: Instant,
    val bid: Double,
    val ask: Double
) {
    val mid: Double get() = (bid + ask) / 2.0
}
