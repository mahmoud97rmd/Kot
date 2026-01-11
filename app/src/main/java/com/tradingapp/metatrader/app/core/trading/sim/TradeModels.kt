package com.tradingapp.metatrader.app.core.trading.sim

enum class Side { BUY, SELL }

data class Position(
    val id: String,
    val side: Side,
    val entryPrice: Double,
    val lots: Double,
    val openedAtSec: Long
)

data class ClosedTrade(
    val id: String,
    val side: Side,
    val entryPrice: Double,
    val exitPrice: Double,
    val lots: Double,
    val openedAtSec: Long,
    val closedAtSec: Long,
    val profit: Double
)
