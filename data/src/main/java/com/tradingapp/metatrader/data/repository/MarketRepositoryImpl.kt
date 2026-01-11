package com.tradingapp.metatrader.data.repository

import com.tradingapp.metatrader.data.local.database.dao.CandleDao
import com.tradingapp.metatrader.data.mappers.toDomain
import com.tradingapp.metatrader.data.mappers.toDomainOrNull
import com.tradingapp.metatrader.data.mappers.toEntity
import com.tradingapp.metatrader.data.remote.api.OandaApiService
import com.tradingapp.metatrader.data.remote.stream.OandaPricingStreamClient
import com.tradingapp.metatrader.domain.models.Candle
import com.tradingapp.metatrader.domain.models.Tick
import com.tradingapp.metatrader.domain.models.Timeframe
import com.tradingapp.metatrader.domain.repository.MarketRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant

class MarketRepositoryImpl(
    private val api: OandaApiService,
    private val candleDao: CandleDao,
    private val stream: OandaPricingStreamClient
) : MarketRepository {

    override suspend fun getHistoricalCandles(
        instrument: String,
        timeframe: Timeframe,
        count: Int
    ): List<Candle> {
        val res = api.getCandles(
            instrument = instrument,
            granularity = timeframe.oandaGranularity,
            count = count
        )
        return res.candles
            .filter { it.complete }
            .mapNotNull { it.toDomainOrNull() }
            .sortedBy { it.time }
    }

    override fun streamTicks(instrument: String): Flow<Tick> = stream.streamTicks(instrument)

    override suspend fun saveCandles(instrument: String, timeframe: Timeframe, candles: List<Candle>) {
        candleDao.upsertAll(candles.map { it.toEntity(instrument, timeframe) })
    }

    override suspend fun getCachedCandles(instrument: String, timeframe: Timeframe, limit: Int): List<Candle> {
        // Room يرجع DESC، نحولها إلى ASC للشارت
        return candleDao.getLatest(instrument, timeframe.name, limit)
            .map { it.toDomain() }
            .sortedBy { it.time }
    }

    override suspend fun getLastCandleTime(instrument: String, timeframe: Timeframe): Instant? {
        return candleDao.getLastTime(instrument, timeframe.name)?.let { Instant.ofEpochSecond(it) }
    }
}
