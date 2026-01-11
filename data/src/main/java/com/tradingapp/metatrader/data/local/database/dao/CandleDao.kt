package com.tradingapp.metatrader.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tradingapp.metatrader.data.local.database.entities.CandleEntity

@Dao
interface CandleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CandleEntity>)

    @Query("""
        SELECT * FROM candles
        WHERE instrument = :instrument AND timeframe = :timeframe
        ORDER BY timeEpochSec DESC
        LIMIT :limit
    """)
    suspend fun getLatest(instrument: String, timeframe: String, limit: Int): List<CandleEntity>

    @Query("""
        SELECT MAX(timeEpochSec) FROM candles
        WHERE instrument = :instrument AND timeframe = :timeframe
    """)
    suspend fun getLastTime(instrument: String, timeframe: String): Long?
}
