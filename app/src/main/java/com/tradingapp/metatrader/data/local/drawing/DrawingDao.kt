package com.tradingapp.metatrader.data.local.drawing

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DrawingDao {

    @Query("SELECT * FROM drawings WHERE instrument = :instrument AND timeframe = :timeframe")
    fun observeByKey(instrument: String, timeframe: String): Flow<List<DrawingEntity>>

    @Query("DELETE FROM drawings WHERE instrument = :instrument AND timeframe = :timeframe")
    suspend fun deleteByKey(instrument: String, timeframe: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<DrawingEntity>)
}
