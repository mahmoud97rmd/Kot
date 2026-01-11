package com.tradingapp.metatrader.app.features.backtest.trades

data class UiTrade(
    val id: String,
    val side: String,
    val entryTimeSec: Long,
    val exitTimeSec: Long,
    val entryPrice: Double,
    val exitPrice: Double,
    val profit: Double,
    val reason: String
)
