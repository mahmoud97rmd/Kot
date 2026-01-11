package com.tradingapp.metatrader.app.core.market.candle

import com.tradingapp.metatrader.app.core.market.MarketCandle
import com.tradingapp.metatrader.app.core.market.MarketTick
import com.tradingapp.metatrader.app.core.time.TimeframeParser

class CandleAggregator(
    private val timeframe: String
) {
    private val tfSec: Long = TimeframeParser.toSeconds(timeframe)

    private var currentOpenSec: Long = -1
    private var current: MarketCandle? = null

    /**
     * Returns:
     * - closed candle (if a new candle started), and
     * - current updated candle
     */
    data class Update(
        val closed: MarketCandle?,
        val current: MarketCandle
    )

    fun reset() {
        currentOpenSec = -1
        current = null
    }

    fun onTick(t: MarketTick): Update {
        val sec = t.timeEpochMs / 1000L
        val openSec = (sec / tfSec) * tfSec
        val mid = (t.bid + t.ask) / 2.0

        val cur = current
        if (cur == null || openSec != currentOpenSec) {
            val closed = cur
            currentOpenSec = openSec
            val newC = MarketCandle(
                timeSec = openSec,
                open = mid,
                high = mid,
                low = mid,
                close = mid,
                volume = 0L
            )
            current = newC
            return Update(closed = closed, current = newC)
        }

        val upd = cur.copy(
            high = maxOf(cur.high, mid),
            low = minOf(cur.low, mid),
            close = mid
        )
        current = upd
        return Update(closed = null, current = upd)
    }
}
