package com.tradingapp.metatrader.core.strategy

import com.tradingapp.metatrader.core.engine.backtest.SimpleBacktestEngine
import com.tradingapp.metatrader.domain.models.Candle
import com.tradingapp.metatrader.domain.models.trading.Position
import kotlin.math.max
import kotlin.math.min

class EmaStochStrategy(
    private val emaFast: Int = 50,
    private val emaSlow: Int = 150,
    private val stochPeriod: Int = 14,
    private val stochTrigger: Double = 20.0,
    private val lots: Double = 1.0,
    private val slPoints: Double = 5.0,   // تعليمية: SL مسافة ثابتة
    private val tpPoints: Double = 10.0   // تعليمية: TP مسافة ثابتة
) {

    private var emaFastValue: Double? = null
    private var emaSlowValue: Double? = null

    private val highs = ArrayDeque<Double>()
    private val lows = ArrayDeque<Double>()

    private var prevK: Double? = null

    fun decide(index: Int, candle: Candle, hasOpen: Boolean): SimpleBacktestEngine.Decision {
        updateEma(candle.close)
        updateStoch(candle.high, candle.low, candle.close)

        val fast = emaFastValue
        val slow = emaSlowValue
        val k = currentK()

        if (fast == null || slow == null || k == null) return SimpleBacktestEngine.Decision.None

        // شرط شراء: EMAfast > EMAslow و K يخترق 20 من أسفل لأعلى
        val signalBuy = (fast > slow) && (prevK != null) && (prevK!! < stochTrigger) && (k > stochTrigger)

        prevK = k

        if (!hasOpen && signalBuy) {
            val entry = candle.close
            val sl = entry - slPoints
            val tp = entry + tpPoints
            return SimpleBacktestEngine.Decision.Open(Position.Side.BUY, lots, sl, tp)
        }

        return SimpleBacktestEngine.Decision.None
    }

    private fun updateEma(close: Double) {
        emaFastValue = emaUpdate(emaFastValue, close, emaFast)
        emaSlowValue = emaUpdate(emaSlowValue, close, emaSlow)
    }

    private fun emaUpdate(prev: Double?, close: Double, period: Int): Double {
        val alpha = 2.0 / (period.toDouble() + 1.0)
        return if (prev == null) close else (close - prev) * alpha + prev
    }

    private var kValue: Double? = null

    private fun updateStoch(high: Double, low: Double, close: Double) {
        highs.addLast(high)
        lows.addLast(low)
        while (highs.size > stochPeriod) highs.removeFirst()
        while (lows.size > stochPeriod) lows.removeFirst()

        if (highs.size < stochPeriod) {
            kValue = null
            return
        }

        var hh = Double.NEGATIVE_INFINITY
        for (h in highs) hh = max(hh, h)

        var ll = Double.POSITIVE_INFINITY
        for (l in lows) ll = min(ll, l)

        val denom = (hh - ll)
        kValue = if (denom <= 0.0) 50.0 else ((close - ll) / denom) * 100.0
    }

    private fun currentK(): Double? = kValue
}
