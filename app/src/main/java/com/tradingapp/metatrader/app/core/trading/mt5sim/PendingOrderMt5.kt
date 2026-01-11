package com.tradingapp.metatrader.app.core.trading.mt5sim

import java.util.UUID

enum class PendingType {
    BUY_LIMIT,
    SELL_LIMIT,
    BUY_STOP,
    SELL_STOP
}

data class PendingOrderMt5(
    val id: String = UUID.randomUUID().toString(),
    val symbol: String,
    val timeframe: String,
    val type: PendingType,
    val lots: Double,
    val entryPrice: Double,
    val stopLoss: Double? = null,
    val takeProfit: Double? = null,
    val comment: String? = null,
    val createdTimeSec: Long
)
