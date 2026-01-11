package com.tradingapp.metatrader.app.core.expert.runtime

class EmaTracker(private val period: Int) {
    private val alpha: Double = 2.0 / (period + 1.0)
    private var initialized = false
    private var ema: Double = 0.0

    fun update(price: Double): Double {
        ema = if (!initialized) {
            initialized = true
            price
        } else {
            (price - ema) * alpha + ema
        }
        return ema
    }

    fun valueOrNull(): Double? = if (initialized) ema else null
}
