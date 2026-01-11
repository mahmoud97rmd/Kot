package com.tradingapp.metatrader.app.features.backtest.metrics

import com.tradingapp.metatrader.domain.models.backtest.BacktestTrade
import kotlin.math.abs
import kotlin.math.max

object ExtendedMetricsCalculator {

    fun calculate(trades: List<BacktestTrade>): ExtendedMetrics {
        val wins = trades.filter { it.profit > 0.0 }
        val losses = trades.filter { it.profit < 0.0 }

        val avgWin = if (wins.isEmpty()) 0.0 else wins.sumOf { it.profit } / wins.size.toDouble()
        val avgLossAbs = if (losses.isEmpty()) 0.0 else losses.sumOf { abs(it.profit) } / losses.size.toDouble()
        val avgLossSigned = if (losses.isEmpty()) 0.0 else losses.sumOf { it.profit } / losses.size.toDouble() // negative

        val maxWin = wins.maxOfOrNull { it.profit } ?: 0.0
        val maxLoss = losses.minOfOrNull { it.profit } ?: 0.0 // most negative

        val payoff = if (avgLossAbs <= 0.0) {
            if (avgWin > 0.0) Double.POSITIVE_INFINITY else 0.0
        } else {
            avgWin / avgLossAbs
        }

        val total = trades.size
        val winRate = if (total == 0) 0.0 else wins.size.toDouble() / total.toDouble()
        val lossRate = 1.0 - winRate

        // Expectancy per trade
        val expectancy = (winRate * avgWin) - (lossRate * avgLossAbs)

        val streaks = computeStreaks(trades)

        return ExtendedMetrics(
            averageWin = avgWin,
            averageLoss = avgLossSigned, // negative (signed)
            maxWin = maxWin,
            maxLoss = maxLoss, // negative
            payoffRatio = payoff,
            expectancy = expectancy,
            longestWinStreak = streaks.longestWin,
            longestLossStreak = streaks.longestLoss,
            consecutiveWinsNow = streaks.currentWin,
            consecutiveLossesNow = streaks.currentLoss
        )
    }

    private data class Streaks(
        val longestWin: Int,
        val longestLoss: Int,
        val currentWin: Int,
        val currentLoss: Int
    )

    private fun computeStreaks(trades: List<BacktestTrade>): Streaks {
        var longestWin = 0
        var longestLoss = 0
        var runWin = 0
        var runLoss = 0

        for (t in trades) {
            when {
                t.profit > 0.0 -> {
                    runWin += 1
                    runLoss = 0
                    longestWin = max(longestWin, runWin)
                }
                t.profit < 0.0 -> {
                    runLoss += 1
                    runWin = 0
                    longestLoss = max(longestLoss, runLoss)
                }
                else -> {
                    // profit == 0 resets both (neutral trade)
                    runWin = 0
                    runLoss = 0
                }
            }
        }

        // current streak at end
        val currentWin = runWin
        val currentLoss = runLoss

        return Streaks(
            longestWin = longestWin,
            longestLoss = longestLoss,
            currentWin = currentWin,
            currentLoss = currentLoss
        )
    }
}
