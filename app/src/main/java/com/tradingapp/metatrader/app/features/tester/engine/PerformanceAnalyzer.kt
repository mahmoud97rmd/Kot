package com.tradingapp.metatrader.app.features.tester.engine

object PerformanceAnalyzer {

    fun maxDrawdown(equity: List<Double>): Double {
        if (equity.isEmpty()) return 0.0
        var peak = equity[0]
        var maxDd = 0.0
        for (v in equity) {
            if (v > peak) peak = v
            val dd = peak - v
            if (dd > maxDd) maxDd = dd
        }
        return maxDd
    }

    fun winRate(profits: List<Double>): Double {
        if (profits.isEmpty()) return 0.0
        val wins = profits.count { it > 0.0 }
        return (wins.toDouble() / profits.size.toDouble()) * 100.0
    }
}
