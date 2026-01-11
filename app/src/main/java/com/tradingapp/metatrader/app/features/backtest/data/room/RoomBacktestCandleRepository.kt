package com.tradingapp.metatrader.app.features.backtest.data.room

import com.tradingapp.metatrader.data.local.backtestdb.dao.CandleDao
import com.tradingapp.metatrader.domain.models.backtest.BacktestCandle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomBacktestCandleRepository @Inject constructor(
    private val dao: CandleDao
) : BacktestCandleRepository {

    override suspend fun getLatest(instrument: String, granularity: String, limit: Int): List<BacktestCandle> {
        return dao.getLatest(instrument, granularity, limit).map(CandleEntityMapper::toDomain)
    }

    override suspend fun getRange(instrument: String, granularity: String, fromSec: Long, toSec: Long): List<BacktestCandle> {
        return dao.getRange(instrument, granularity, fromSec, toSec).map(CandleEntityMapper::toDomain)
    }

    override suspend fun count(instrument: String, granularity: String): Long {
        return dao.count(instrument, granularity)
    }
}
