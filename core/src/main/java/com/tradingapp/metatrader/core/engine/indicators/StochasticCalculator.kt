package com.tradingapp.metatrader.core.engine.indicators

import java.util.ArrayDeque
import kotlin.math.max
import kotlin.math.min

/**
 * Stochastic %K:
 * %K = 100 * (Close - LowestLow(n)) / (HighestHigh(n) - LowestLow(n))
 *
 * هذا الإصدار يحافظ على نافذة شموع (high/low/close) ويحسب K,
 * ويمكنك إضافة smoothing لاحقًا (%D).
 */
class StochasticCalculator(
    private val lookback: Int = 14
) {
    data class Bar(val high: Double, val low: Double, val close: Double)

    private val window = ArrayDeque<Bar>(lookback)

    fun update(high: Double, low: Double, close: Double): Double? {
        if (window.size >= lookback) window.removeFirst()
        window.addLast(Bar(high, low, close))

        if (window.size < lookback) return null

        var hh = Double.NEGATIVE_INFINITY
        var ll = Double.POSITIVE_INFINITY
        for (b in window) {
            hh = max(hh, b.high)
            ll = min(ll, b.low)
        }
        val denom = (hh - ll)
        if (denom == 0.0) return 50.0
        return 100.0 * (close - ll) / denom
    }
}
