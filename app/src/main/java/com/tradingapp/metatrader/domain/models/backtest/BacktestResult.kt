package com.tradingapp.metatrader.domain.models.backtest

data class BacktestResult(
    val config: BacktestConfig,
    val totalTrades: Int,
    val winRate: Double,
    val netProfit: Double,
    val maxDrawdown: Double,
    val trades: List<BacktestTrade>,
    val equityCurve: List<EquityPoint>
)
