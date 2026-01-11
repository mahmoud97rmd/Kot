package com.tradingapp.metatrader.domain.models.backtest

data class BacktestResult(
    val config: BacktestConfig,
    val trades: List<BacktestTrade>,
    val equityCurve: List<EquityPoint>,
    val metrics: PerformanceMetrics
)
