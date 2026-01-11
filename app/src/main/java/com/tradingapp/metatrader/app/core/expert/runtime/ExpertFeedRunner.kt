package com.tradingapp.metatrader.app.core.expert.runtime

import com.tradingapp.metatrader.app.core.feed.CandleFeed
import com.tradingapp.metatrader.app.core.feed.CandleUpdate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ExpertFeedRunner(
    private val feed: CandleFeed,
    private val runtime: ExpertRuntime
) {
    /**
     * Streams ExpertRuntime events produced by processing candles from feed.
     */
    fun run(symbol: String, timeframe: String): Flow<ExpertRuntime.Event> = flow {
        feed.stream(symbol, timeframe).collect { upd ->
            if (upd is CandleUpdate.Current) {
                val events = runtime.onCandle(symbol, timeframe, upd.candle)
                for (e in events) emit(e)
            }
        }
    }
}
