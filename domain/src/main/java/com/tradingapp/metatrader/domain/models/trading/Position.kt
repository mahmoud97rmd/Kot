package com.tradingapp.metatrader.domain.models.trading

import java.time.Instant

data class Position(
    val id: String,
    val instrument: String,
    val side: Side,
    val entryTime: Instant,
    val entryPrice: Double,
    val lots: Double,
    val stopLoss: Double?,
    val takeProfit: Double?,
    val comment: String?
) {
    enum class Side { BUY, SELL }
}
