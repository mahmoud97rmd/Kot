package com.tradingapp.metatrader.core.engine.indicators

/**
 * EMA تراكمي:
 * EMA = (Close - PrevEMA)*k + PrevEMA, حيث k = 2/(period+1)
 */
class EMACalculator(private val period: Int) {
    private val k: Double = 2.0 / (period + 1.0)
    private var initialized = false
    private var ema: Double = 0.0

    fun update(close: Double): Double {
        if (!initialized) {
            ema = close
            initialized = true
            return ema
        }
        ema = (close - ema) * k + ema
        return ema
    }

    fun valueOrNull(): Double? = if (initialized) ema else null
}
