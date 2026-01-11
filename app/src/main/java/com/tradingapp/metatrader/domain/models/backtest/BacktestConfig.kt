package com.tradingapp.metatrader.domain.models.backtest

data class BacktestConfig(
    val initialBalance: Double = 10_000.0,
    val pointValue: Double = 0.01,
    val commissionPerLot: Double = 0.0,
    val spreadPoints: Double = 0.0
)
