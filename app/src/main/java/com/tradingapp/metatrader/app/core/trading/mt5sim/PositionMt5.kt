package com.tradingapp.metatrader.app.core.trading.mt5sim

data class PositionMt5(
    val id: String,
    val symbol: String,
    val side: Side,
    val lots: Double,
    val entryPrice: Double,
    val openTimeSec: Long,
    val stopLoss: Double? = null,
    val takeProfit: Double? = null,
    val trailingStopPips: Double? = null,
    val comment: String? = null
)
