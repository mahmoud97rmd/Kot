package com.tradingapp.metatrader.domain.models.trading

import java.time.Instant

data class ClosedTrade(
    val id: String,
    val instrument: String,
    val side: Position.Side,
    val entryTime: Instant,
    val exitTime: Instant,
    val entryPrice: Double,
    val exitPrice: Double,
    val lots: Double,
    val profit: Double,
    val comment: String?
)
