package com.tradingapp.metatrader.app.data.local.cache

import com.tradingapp.metatrader.app.core.candles.Candle
import com.tradingapp.metatrader.app.core.candles.CandleMappers
import com.tradingapp.metatrader.app.core.market.MarketCandle
import com.tradingapp.metatrader.app.data.local.db.dao.CandleDao
import com.tradingapp.metatrader.app.data.local.db.entities.CandleEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CandleCacheRepository @Inject constructor(
    private val dao: CandleDao
) {
    // ---------- Existing API (MarketCandle) ----------
    suspend fun loadRecent(symbol: String, timeframe: String, limit: Int): List<MarketCandle> {
        return loadRecentUnified(symbol, timeframe, limit).map { CandleMappers.toMarket(it) }
    }

    suspend fun getLastTimeSec(symbol: String, timeframe: String): Long? {
        return dao.getMaxTimeSec(symbol, timeframe)
    }

    suspend fun upsert(symbol: String, timeframe: String, candles: List<MarketCandle>) {
        upsertUnified(symbol, timeframe, candles.map { CandleMappers.fromMarket(it) })
    }

    suspend fun trimKeepLast(symbol: String, timeframe: String, keepCount: Int) {
        val keep = keepCount.coerceIn(100, 20000)
        val recent = dao.getRecent(symbol, timeframe, keep)
        if (recent.isEmpty()) return
        val oldestKept = recent.last().timeSec
        dao.deleteOlderThan(symbol, timeframe, oldestKept)
    }

    // ---------- New Unified API (Candle) ----------
    suspend fun loadRecentUnified(symbol: String, timeframe: String, limit: Int): List<Candle> {
        val rows = dao.getRecent(symbol, timeframe, limit.coerceIn(1, 5000))
        return rows.asReversed().map { it.toUnified() }
    }

    suspend fun upsertUnified(symbol: String, timeframe: String, candles: List<Candle>) {
        if (candles.isEmpty()) return
        val items = candles.map { it.toEntity(symbol, timeframe) }
        dao.upsertAll(items)
    }

    private fun CandleEntity.toUnified(): Candle {
        return Candle(
            timeSec = timeSec,
            open = open,
            high = high,
            low = low,
            close = close,
            volume = volume
        )
    }

    private fun Candle.toEntity(symbol: String, timeframe: String): CandleEntity {
        return CandleEntity(
            symbol = symbol,
            timeframe = timeframe,
            timeSec = timeSec,
            open = open,
            high = high,
            low = low,
            close = close,
            volume = volume
        )
    }
}
