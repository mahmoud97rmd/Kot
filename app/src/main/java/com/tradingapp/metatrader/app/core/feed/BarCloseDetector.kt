package com.tradingapp.metatrader.app.core.feed

import com.tradingapp.metatrader.app.core.candles.Candle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Converts a stream of CandleUpdate.Current updates into "closed bars" (Candle) emitted once per bar.
 *
 * Logic:
 * - receives Current(candle) repeatedly while the same candle is forming
 * - when timeSec changes, previous candle is considered closed and emitted once
 */
object BarCloseDetector {

    fun closedBars(updates: Flow<CandleUpdate>): Flow<Candle> = flow {
        var last: Candle? = null
        updates.collect { u ->
            if (u is CandleUpdate.Current) {
                val cur = u.candle
                val prev = last
                if (prev == null) {
                    last = cur
                    return@collect
                }
                if (cur.timeSec != prev.timeSec) {
                    // previous candle is now closed
                    emit(prev)
                }
                last = cur
            } else if (u is CandleUpdate.History) {
                // optional: you can emit last history candle as "closed" if needed
            }
        }
    }
}
