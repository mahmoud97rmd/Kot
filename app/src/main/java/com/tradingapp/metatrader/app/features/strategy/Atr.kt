package com.tradingapp.metatrader.app.features.strategy

import com.tradingapp.metatrader.domain.models.Candle
import kotlin.math.abs
import kotlin.math.max

object Atr {

    /**
     * True Range = max(high-low, abs(high-prevClose), abs(low-prevClose))
     * ATR (Wilder) simplified: average of TR over period.
     */
    fun atr(candles: List<Candle>, period: Int = 14): Double? {
        if (period <= 0) return null
        if (candles.size < period + 1) return null

        val recent = candles.takeLast(period + 1)
        var sum = 0.0
        for (i in 1 until recent.size) {
            val cur = recent[i]
            val prev = recent[i - 1]
            val tr = trueRange(cur.high, cur.low, prev.close)
            sum += tr
        }
        return sum / period.toDouble()
    }

    private fun trueRange(high: Double, low: Double, prevClose: Double): Double {
        val a = high - low
        val b = abs(high - prevClose)
        val c = abs(low - prevClose)
        return max(a, max(b, c))
    }
}
