package com.tradingapp.metatrader.app.features.backtest.metrics

data class ExtendedMetrics(
    val averageWin: Double,
    val averageLoss: Double,
    val maxWin: Double,
    val maxLoss: Double,
    val payoffRatio: Double,     // AvgWin / AvgLossAbs
    val expectancy: Double,      // (WinRate*AvgWin) - (LossRate*AvgLossAbs)
    val longestWinStreak: Int,
    val longestLossStreak: Int,
    val consecutiveWinsNow: Int,
    val consecutiveLossesNow: Int
)
