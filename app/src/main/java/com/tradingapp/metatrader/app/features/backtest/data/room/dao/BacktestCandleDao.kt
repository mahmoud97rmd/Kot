package com.tradingapp.metatrader.app.features.backtest.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tradingapp.metatrader.app.features.backtest.data.room.entities.BacktestCandleEntity

@Dao
interface BacktestCandleDao {

    @Query("SELECT COUNT(*) FROM backtest_candles WHERE instrument = :instrument AND granularity = :granularity")
    suspend fun count(instrument: String, granularity: String): Int

    @Query("""
        SELECT * FROM backtest_candles
        WHERE instrument = :instrument AND granularity = :granularity
        ORDER BY timeSec DESC
        LIMIT :limit
    """)
    suspend fun getLatestDesc(instrument: String, granularity: String, limit: Int): List<BacktestCandleEntity>

    @Query("""
        SELECT * FROM backtest_candles
        WHERE instrument = :instrument AND granularity = :granularity
          AND timeSec BETWEEN :fromSec AND :toSec
        ORDER BY timeSec ASC
    """)
    suspend fun getRangeAsc(instrument: String, granularity: String, fromSec: Long, toSec: Long): List<BacktestCandleEntity>

    @Query("""
        SELECT timeSec FROM backtest_candles
        WHERE instrument = :instrument AND granularity = :granularity
          AND timeSec BETWEEN :fromSec AND :toSec
        ORDER BY timeSec ASC
    """)
    suspend fun getTimesAsc(instrument: String, granularity: String, fromSec: Long, toSec: Long): List<Long>

    @Query("""
        SELECT MIN(timeSec) FROM backtest_candles
        WHERE instrument = :instrument AND granularity = :granularity
    """)
    suspend fun minTime(instrument: String, granularity: String): Long?

    @Query("""
        SELECT MAX(timeSec) FROM backtest_candles
        WHERE instrument = :instrument AND granularity = :granularity
    """)
    suspend fun maxTime(instrument: String, granularity: String): Long?

    @Query("""
        SELECT COUNT(*) FROM backtest_candles
        WHERE instrument = :instrument AND granularity = :granularity
          AND timeSec BETWEEN :fromSec AND :toSec
    """)
    suspend fun countRange(instrument: String, granularity: String, fromSec: Long, toSec: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<BacktestCandleEntity>)

    @Query("DELETE FROM backtest_candles WHERE instrument = :instrument AND granularity = :granularity")
    suspend fun deleteFor(instrument: String, granularity: String)
}
