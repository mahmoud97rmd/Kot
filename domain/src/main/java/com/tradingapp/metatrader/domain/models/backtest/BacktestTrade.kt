package com.tradingapp.metatrader.domain.models.backtest

data class BacktestTrade(
    val id: String,
    val side: BacktestSide,
    val lots: Double,
    val entryPrice: Double,
    val entryTimeSec: Long,
    val exitPrice: Double,
    val exitTimeSec: Long,
    val profit: Double,
    val stopLoss: Double? = null,
    val takeProfit: Double? = null
)
