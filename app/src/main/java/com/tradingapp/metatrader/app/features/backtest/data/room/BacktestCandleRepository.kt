package com.tradingapp.metatrader.app.features.backtest.data.room

import com.tradingapp.metatrader.app.features.backtest.data.room.dao.BacktestCandleDao
import com.tradingapp.metatrader.app.features.backtest.data.room.entities.BacktestCandleEntity
import com.tradingapp.metatrader.domain.models.backtest.BacktestCandle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BacktestCandleRepository @Inject constructor(
    private val dao: BacktestCandleDao
) {
    suspend fun count(instrument: String, granularity: String): Int =
        dao.count(instrument, granularity)

    suspend fun minTime(instrument: String, granularity: String): Long? =
        dao.minTime(instrument, granularity)

    suspend fun maxTime(instrument: String, granularity: String): Long? =
        dao.maxTime(instrument, granularity)

    suspend fun countRange(instrument: String, granularity: String, fromSec: Long, toSec: Long): Int =
        dao.countRange(instrument, granularity, fromSec, toSec)

    suspend fun getTimesAsc(instrument: String, granularity: String, fromSec: Long, toSec: Long): List<Long> =
        dao.getTimesAsc(instrument, granularity, fromSec, toSec)

    suspend fun getLatest(instrument: String, granularity: String, limit: Int): List<BacktestCandle> =
        dao.getLatestDesc(instrument, granularity, limit)
            .asReversed()
            .map { it.toDomain() }

    suspend fun getRange(instrument: String, granularity: String, fromSec: Long, toSec: Long): List<BacktestCandle> =
        dao.getRangeAsc(instrument, granularity, fromSec, toSec).map { it.toDomain() }

    suspend fun upsertAll(instrument: String, granularity: String, candles: List<BacktestCandle>) {
        dao.upsertAll(candles.map { it.toEntity(instrument, granularity) })
    }

    suspend fun replaceAll(instrument: String, granularity: String, candles: List<BacktestCandle>) {
        dao.deleteFor(instrument, granularity)
        dao.upsertAll(candles.map { it.toEntity(instrument, granularity) })
    }

    private fun BacktestCandleEntity.toDomain(): BacktestCandle =
        BacktestCandle(timeSec = timeSec, open = open, high = high, low = low, close = close)

    private fun BacktestCandle.toEntity(instrument: String, granularity: String): BacktestCandleEntity =
        BacktestCandleEntity(
            instrument = instrument,
            granularity = granularity,
            timeSec = timeSec,
            open = open, high = high, low = low, close = close
        )
}
