package com.tradingapp.metatrader.domain.repository

import com.tradingapp.metatrader.domain.models.Candle
import com.tradingapp.metatrader.domain.models.Tick
import com.tradingapp.metatrader.domain.models.Timeframe
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface MarketRepository {
    suspend fun getHistoricalCandles(
        instrument: String,
        timeframe: Timeframe,
        count: Int = 500
    ): List<Candle>

    fun streamTicks(instrument: String): Flow<Tick>

    suspend fun saveCandles(instrument: String, timeframe: Timeframe, candles: List<Candle>)
    suspend fun getCachedCandles(instrument: String, timeframe: Timeframe, limit: Int = 500): List<Candle>

    suspend fun getLastCandleTime(instrument: String, timeframe: Timeframe): Instant?
}
