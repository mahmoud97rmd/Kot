package com.tradingapp.metatrader.app.features.backtest.inputs

enum class StrategyType { NONE }
enum class ModelingMode { BARS_ONLY }

data class BacktestInputs(
    val strategyType: StrategyType = StrategyType.NONE,
    val modelingMode: ModelingMode = ModelingMode.BARS_ONLY,
    val initialBalance: Double = 10_000.0,
    val spreadPoints: Double = 0.0,
    val commissionPerLot: Double = 0.0,
    val pointValue: Double = 0.01
)
