package com.tradingapp.metatrader.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tradingapp.metatrader.app.data.local.db.entities.CandleEntity

@Dao
interface CandleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CandleEntity>)

    @Query("""
        SELECT * FROM candles
        WHERE symbol = :symbol AND timeframe = :timeframe
        ORDER BY timeSec DESC
        LIMIT :limit
    """)
    suspend fun getRecent(symbol: String, timeframe: String, limit: Int): List<CandleEntity>

    @Query("""
        SELECT MAX(timeSec) FROM candles
        WHERE symbol = :symbol AND timeframe = :timeframe
    """)
    suspend fun getMaxTimeSec(symbol: String, timeframe: String): Long?

    @Query("""
        DELETE FROM candles
        WHERE symbol = :symbol AND timeframe = :timeframe AND timeSec < :minTimeSec
    """)
    suspend fun deleteOlderThan(symbol: String, timeframe: String, minTimeSec: Long)

    @Query("""
        SELECT COUNT(*) FROM candles
        WHERE symbol = :symbol AND timeframe = :timeframe
    """)
    suspend fun count(symbol: String, timeframe: String): Int
}
