package com.tradingapp.metatrader.domain.backtest

import com.tradingapp.metatrader.domain.models.backtest.BacktestCandle
import com.tradingapp.metatrader.domain.models.backtest.BacktestSignal
import com.tradingapp.metatrader.domain.models.backtest.BacktestSide
import kotlin.math.abs
import kotlin.math.max

/**
 * RSI Reversal:
 * BUY when RSI crosses above oversold (prev < OS && curr > OS)
 * SELL when RSI crosses below overbought (prev > OB && curr < OB)
 */
class RsiReversalStrategy(
    private val period: Int = 14,
    private val oversold: Double = 30.0,
    private val overbought: Double = 70.0,
    private val lots: Double = 1.0
) : BacktestStrategy {

    private val p = max(1, period)

    private var prevClose: Double? = null
    private var avgGain: Double? = null
    private var avgLoss: Double? = null
    private var lastRsi: Double? = null

    override fun onCandleClosed(history: List<BacktestCandle>): BacktestSignal? {
        val close = history.last().close
        val prev = prevClose
        prevClose = close

        if (prev == null) return null

        val change = close - prev
        val gain = if (change > 0) change else 0.0
        val loss = if (change < 0) abs(change) else 0.0

        // Initialize using first p samples
        if (avgGain == null || avgLoss == null) {
            // We need enough history to initialize: p+1 closes at least
            if (history.size < p + 1) return null

            var sumG = 0.0
            var sumL = 0.0
            val start = history.size - (p + 1)
            for (i in start + 1 until history.size) {
                val c0 = history[i - 1].close
                val c1 = history[i].close
                val d = c1 - c0
                if (d > 0) sumG += d else sumL += abs(d)
            }
            avgGain = sumG / p.toDouble()
            avgLoss = sumL / p.toDouble()
        } else {
            // Wilder smoothing
            avgGain = ((avgGain!! * (p - 1)) + gain) / p.toDouble()
            avgLoss = ((avgLoss!! * (p - 1)) + loss) / p.toDouble()
        }

        val rsi = calcRsi(avgGain!!, avgLoss!!)
        val prevRsi = lastRsi
        lastRsi = rsi

        if (prevRsi == null) return null

        // Cross rules
        return if (prevRsi < oversold && rsi > oversold) {
            BacktestSignal(side = BacktestSide.BUY, lots = lots)
        } else if (prevRsi > overbought && rsi < overbought) {
            BacktestSignal(side = BacktestSide.SELL, lots = lots)
        } else null
    }

    private fun calcRsi(avgG: Double, avgL: Double): Double {
        if (avgL <= 0.0) return 100.0
        val rs = avgG / avgL
        return 100.0 - (100.0 / (1.0 + rs))
    }
}
