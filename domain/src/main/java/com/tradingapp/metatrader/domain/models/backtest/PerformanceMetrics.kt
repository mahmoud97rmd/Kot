package com.tradingapp.metatrader.domain.models.backtest

data class PerformanceMetrics(
    val netProfit: Double,
    val grossProfit: Double,
    val grossLoss: Double,
    val winRate: Double,
    val totalTrades: Int,
    val maxDrawdown: Double,
    val profitFactor: Double,

    // MT5-like extras
    val expectedPayoff: Double,     // netProfit / totalTrades
    val recoveryFactor: Double,     // netProfit / maxDrawdown
    val sharpeLike: Double          // approximate Sharpe-like ratio
)
