package com.tradingapp.metatrader.core.backtest

import com.tradingapp.metatrader.domain.models.backtest.BacktestTrade
import com.tradingapp.metatrader.domain.models.backtest.PerformanceMetrics
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object PerformanceAnalyzer {

    fun analyze(trades: List<BacktestTrade>, equity: List<Double>, initialBalance: Double): PerformanceMetrics {
        val netProfit = trades.sumOf { it.profit }
        val grossProfit = trades.filter { it.profit > 0 }.sumOf { it.profit }
        val grossLoss = trades.filter { it.profit < 0 }.sumOf { abs(it.profit) }

        val wins = trades.count { it.profit > 0 }
        val total = trades.size
        val winRate = if (total == 0) 0.0 else wins.toDouble() / total.toDouble()

        val pf = if (grossLoss <= 0.0) {
            if (grossProfit > 0.0) Double.POSITIVE_INFINITY else 0.0
        } else grossProfit / grossLoss

        val maxDrawdown = maxDrawdownFromEquity(equity, initialBalance)

        return PerformanceMetrics(
            netProfit = netProfit,
            grossProfit = grossProfit,
            grossLoss = grossLoss,
            winRate = winRate,
            totalTrades = total,
            maxDrawdown = maxDrawdown,
            profitFactor = pf
        )
    }

    private fun maxDrawdownFromEquity(equity: List<Double>, initial: Double): Double {
        if (equity.isEmpty()) return 0.0

        var peak = initial
        var maxDd = 0.0
        for (v in equity) {
            peak = max(peak, v)
            val dd = peak - v
            maxDd = max(maxDd, dd)
        }
        return maxDd
    }
}
