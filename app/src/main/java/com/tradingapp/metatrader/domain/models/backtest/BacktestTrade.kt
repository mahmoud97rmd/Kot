package com.tradingapp.metatrader.domain.models.backtest

data class BacktestTrade(
    val id: String,
    val side: String, // "BUY" or "SELL"
    val entryTimeSec: Long,
    val exitTimeSec: Long,
    val entryPrice: Double,
    val exitPrice: Double,
    val profit: Double,
    val reason: String
)
