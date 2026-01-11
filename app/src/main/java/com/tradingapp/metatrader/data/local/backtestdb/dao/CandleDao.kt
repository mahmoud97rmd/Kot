package com.tradingapp.metatrader.data.local.backtestdb.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tradingapp.metatrader.data.local.backtestdb.entities.CandleEntity

@Dao
interface CandleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CandleEntity>)

    @Query("""
        SELECT * FROM candles
        WHERE instrument = :instrument
          AND granularity = :granularity
        ORDER BY timeSec ASC
        LIMIT :limit
    """)
    suspend fun getLatest(
        instrument: String,
        granularity: String,
        limit: Int
    ): List<CandleEntity>

    @Query("""
        SELECT * FROM candles
        WHERE instrument = :instrument
          AND granularity = :granularity
          AND timeSec BETWEEN :fromSec AND :toSec
        ORDER BY timeSec ASC
    """)
    suspend fun getRange(
        instrument: String,
        granularity: String,
        fromSec: Long,
        toSec: Long
    ): List<CandleEntity>

    @Query("""
        SELECT COUNT(*) FROM candles
        WHERE instrument = :instrument AND granularity = :granularity
    """)
    suspend fun count(
        instrument: String,
        granularity: String
    ): Long
}
