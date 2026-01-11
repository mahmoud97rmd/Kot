package com.tradingapp.metatrader.domain.models.backtest

data class BacktestSignal(
    val side: BacktestSide,
    val lots: Double,
    val stopLoss: Double? = null,
    val takeProfit: Double? = null
)
