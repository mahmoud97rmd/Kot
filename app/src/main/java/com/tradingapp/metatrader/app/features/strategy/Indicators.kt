package com.tradingapp.metatrader.app.features.strategy

import com.tradingapp.metatrader.domain.models.Candle
import kotlin.math.max
import kotlin.math.min

object Indicators {

    fun ema(closes: List<Double>, period: Int): Double? {
        if (closes.isEmpty() || period <= 0) return null
        var ema: Double? = null
        val alpha = 2.0 / (period.toDouble() + 1.0)
        for (c in closes) {
            ema = if (ema == null) c else (c - ema) * alpha + ema
        }
        return ema
    }

    /**
     * Stoch %K last value over last period candles.
     * Returns null if insufficient candles.
     */
    fun stochasticK(candles: List<Candle>, period: Int): Double? {
        if (period <= 0) return null
        if (candles.size < period) return null
        val window = candles.takeLast(period)
        var hh = Double.NEGATIVE_INFINITY
        var ll = Double.POSITIVE_INFINITY
        for (c in window) {
            hh = max(hh, c.high)
            ll = min(ll, c.low)
        }
        val denom = (hh - ll)
        if (denom <= 0.0) return 50.0
        val close = window.last().close
        return ((close - ll) / denom) * 100.0
    }
}
