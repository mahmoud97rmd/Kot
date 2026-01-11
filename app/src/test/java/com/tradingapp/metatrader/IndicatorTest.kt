package com.tradingapp.metatrader

import com.tradingapp.metatrader.app.core.candles.Candle
import com.tradingapp.metatrader.app.core.indicators.EMACalculator
import com.tradingapp.metatrader.app.core.indicators.StochasticCalculator
import org.junit.Assert.*
import org.junit.Test

class IndicatorTest {

    @Test
    fun ema_converges() {
        val ema = EMACalculator(10)
        var last = 0.0
        for (i in 1..100) {
            last = ema.update(100.0)
        }
        assertTrue(last > 99.0 && last <= 100.0)
    }

    @Test
    fun stoch_in_range() {
        val st = StochasticCalculator(14, 3)
        var t = 1L
        var outCount = 0
        for (i in 1..30) {
            val c = Candle(
                timeSec = t++,
                open = 100.0,
                high = 110.0,
                low = 90.0,
                close = 95.0 + i,
                volume = 0.0
            )
            val v = st.update(c)
            if (v != null) {
                outCount++
                assertTrue(v.k in 0.0..100.0)
                assertTrue(v.d in 0.0..100.0)
            }
        }
        assertTrue(outCount > 0)
    }
}
