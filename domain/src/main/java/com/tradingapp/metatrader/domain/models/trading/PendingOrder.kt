package com.tradingapp.metatrader.domain.models.trading

import java.time.Instant

data class PendingOrder(
    val id: String,
    val instrument: String,
    val type: Type,
    val createdAt: Instant,
    val targetPrice: Double,
    val lots: Double,
    val stopLoss: Double?,
    val takeProfit: Double?,
    val comment: String?
) {
    enum class Type {
        BUY_LIMIT,
        SELL_LIMIT,
        BUY_STOP,
        SELL_STOP
    }
}
