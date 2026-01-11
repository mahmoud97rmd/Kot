package com.tradingapp.metatrader.domain.models.backtest

data class BacktestConfig(
    val initialBalance: Double,
    val commissionPerLot: Double,
    val spreadPoints: Double,
    val slippagePoints: Double,
    val pointValue: Double,
    val modelingMode: ModelingMode = ModelingMode.CANDLE_EXTREMES,

    // MT5-like risk/levels (optional)
    val stopLossPoints: Double = 0.0,
    val takeProfitPoints: Double = 0.0,
    val riskPercent: Double = 0.0
)
