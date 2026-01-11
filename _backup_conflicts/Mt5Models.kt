package com.tradingapp.metatrader.app.core.trading.mt5sim

enum class Side { BUY, SELL }

data class PositionMt5(
    val id: String,
    val symbol: String,
    val side: Side,
    var lots: Double,
    val entryPrice: Double,
    val openedAtSec: Long,
    var stopLoss: Double? = null,
    var takeProfit: Double? = null,
    var trailingStopPips: Double? = null
)

data class DealMt5(
    val id: String,
    val positionId: String,
    val symbol: String,
    val side: Side,
    val lots: Double,
    val entryPrice: Double,
    val exitPrice: Double,
    val openedAtSec: Long,
    val closedAtSec: Long,
    val profit: Double,
    val commission: Double,
    val reason: CloseReason
)

enum class CloseReason {
    MANUAL,
    STOP_LOSS,
    TAKE_PROFIT,
    TRAILING_STOP
}
