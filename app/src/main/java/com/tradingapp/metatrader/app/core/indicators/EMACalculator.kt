package com.tradingapp.metatrader.app.core.indicators

class EMACalculator(private val period: Int) {

    init {
        require(period > 0) { "EMA period must be > 0" }
    }

    private val multiplier: Double = 2.0 / (period + 1.0)
    private var ema: Double? = null

    fun reset() {
        ema = null
    }

    fun update(close: Double): Double {
        val prev = ema
        val next = if (prev == null) {
            close
        } else {
            (close - prev) * multiplier + prev
        }
        ema = next
        return next
    }

    fun valueOrNull(): Double? = ema
}
