package com.tradingapp.metatrader.domain.backtest

import com.tradingapp.metatrader.domain.models.backtest.BacktestCandle
import com.tradingapp.metatrader.domain.models.backtest.BacktestSignal
import com.tradingapp.metatrader.domain.models.backtest.BacktestSide
import kotlin.math.max
import kotlin.math.min

/**
 * Stochastic %K/%D cross + threshold:
 * BUY when %K crosses above oversold (prevK < OS && currK > OS)
 * SELL when %K crosses below overbought (prevK > OB && currK < OB)
 *
 * %D is calculated but currently optional for triggering; can be used later.
 */
class StochasticCrossStrategy(
    private val kPeriod: Int = 14,
    private val dPeriod: Int = 3,
    private val oversold: Double = 20.0,
    private val overbought: Double = 80.0,
    private val lots: Double = 1.0
) : BacktestStrategy {

    private val kP = max(1, kPeriod)
    private val dP = max(1, dPeriod)

    private val kHistory = ArrayDeque<Double>(50)
    private var lastK: Double? = null

    override fun onCandleClosed(history: List<BacktestCandle>): BacktestSignal? {
        if (history.size < kP) return null

        val window = history.takeLast(kP)
        var hh = window[0].high
        var ll = window[0].low
        for (c in window) {
            hh = max(hh, c.high)
            ll = min(ll, c.low)
        }

        val close = history.last().close
        val k = if ((hh - ll) <= 0.0) 50.0 else ((close - ll) / (hh - ll)) * 100.0
        pushK(k)
        val d = smaD()

        val prevK = lastK
        lastK = k

        if (prevK == null) return null

        // Cross threshold rules (simple and robust)
        return if (prevK < oversold && k > oversold) {
            BacktestSignal(side = BacktestSide.BUY, lots = lots)
        } else if (prevK > overbought && k < overbought) {
            BacktestSignal(side = BacktestSide.SELL, lots = lots)
        } else {
            null
        }
    }

    private fun pushK(v: Double) {
        if (kHistory.size >= 200) kHistory.removeFirst()
        kHistory.addLast(v)
    }

    private fun smaD(): Double {
        if (kHistory.isEmpty()) return 50.0
        val n = min(dP, kHistory.size)
        var sum = 0.0
        val it = kHistory.takeLast(n)
        for (v in it) sum += v
        return sum / n.toDouble()
    }
}
