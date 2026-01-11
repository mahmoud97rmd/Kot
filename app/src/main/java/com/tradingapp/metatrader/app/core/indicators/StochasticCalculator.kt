package com.tradingapp.metatrader.app.core.indicators

import com.tradingapp.metatrader.app.core.candles.Candle
import kotlin.math.max
import kotlin.math.min

class StochasticCalculator(
    private val kPeriod: Int,
    private val dPeriod: Int
) {
    init {
        require(kPeriod > 0) { "kPeriod must be > 0" }
        require(dPeriod > 0) { "dPeriod must be > 0" }
    }

    private val window = ArrayDeque<Candle>()
    private val kQueue = ArrayDeque<Double>()

    fun reset() {
        window.clear()
        kQueue.clear()
    }

    data class Value(val k: Double, val d: Double)

    fun update(candle: Candle): Value? {
        window.addLast(candle)
        if (window.size > kPeriod) window.removeFirst()

        if (window.size < kPeriod) return null

        var highestHigh = Double.NEGATIVE_INFINITY
        var lowestLow = Double.POSITIVE_INFINITY
        for (c in window) {
            highestHigh = max(highestHigh, c.high)
            lowestLow = min(lowestLow, c.low)
        }

        val denom = (highestHigh - lowestLow)
        val k = if (denom <= 0.0) 50.0 else ((candle.close - lowestLow) / denom) * 100.0
        val kClamped = k.coerceIn(0.0, 100.0)

        kQueue.addLast(kClamped)
        if (kQueue.size > dPeriod) kQueue.removeFirst()

        if (kQueue.size < dPeriod) return Value(kClamped, kClamped)

        val d = kQueue.sum() / kQueue.size.toDouble()
        return Value(kClamped, d.coerceIn(0.0, 100.0))
    }
}
