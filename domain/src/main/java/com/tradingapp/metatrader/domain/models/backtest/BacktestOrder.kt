package com.tradingapp.metatrader.domain.models.backtest

data class BacktestOrder(
    val side: BacktestSide,
    val lots: Double,
    val entryPrice: Double,
    val stopLoss: Double? = null,
    val takeProfit: Double? = null,
    val timeSec: Long
)
