package com.tradingapp.metatrader.domain.backtest

import com.tradingapp.metatrader.domain.models.backtest.BacktestCandle
import com.tradingapp.metatrader.domain.models.backtest.BacktestSignal
import com.tradingapp.metatrader.domain.models.backtest.BacktestSide
import kotlin.math.max

class SimpleEmaCrossStrategy(
    private val fast: Int = 10,
    private val slow: Int = 30,
    private val lots: Double = 1.0
) : BacktestStrategy {

    private var emaFast: Double? = null
    private var emaSlow: Double? = null
    private var lastSide: BacktestSide? = null

    override fun onCandleClosed(history: List<BacktestCandle>): BacktestSignal? {
        val close = history.last().close
        emaFast = ema(emaFast, close, fast)
        emaSlow = ema(emaSlow, close, slow)

        val f = emaFast ?: return null
        val s = emaSlow ?: return null

        // generate a new signal only on cross to avoid spamming
        return if (f > s && lastSide != BacktestSide.BUY) {
            lastSide = BacktestSide.BUY
            BacktestSignal(side = BacktestSide.BUY, lots = lots)
        } else if (f < s && lastSide != BacktestSide.SELL) {
            lastSide = BacktestSide.SELL
            BacktestSignal(side = BacktestSide.SELL, lots = lots)
        } else {
            null
        }
    }

    private fun ema(prev: Double?, close: Double, period: Int): Double {
        val p = max(1, period)
        val k = 2.0 / (p + 1.0)
        return if (prev == null) close else (close - prev) * k + prev
    }
}
