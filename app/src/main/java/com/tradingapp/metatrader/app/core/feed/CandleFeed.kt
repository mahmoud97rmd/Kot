package com.tradingapp.metatrader.app.core.feed

import com.tradingapp.metatrader.app.core.candles.Candle
import kotlinx.coroutines.flow.Flow

sealed class CandleUpdate {
    data class History(val candles: List<Candle>) : CandleUpdate()
    data class Current(val candle: Candle) : CandleUpdate()
    data class Closed(val candle: Candle) : CandleUpdate()
    data class Status(val text: String) : CandleUpdate()
}

interface CandleFeed {
    /**
     * Emits:
     * - Status(...)
     * - History(candles)
     * - Current(...) updates (and optionally Closed(...))
     */
    fun stream(symbol: String, timeframe: String): Flow<CandleUpdate>
}
