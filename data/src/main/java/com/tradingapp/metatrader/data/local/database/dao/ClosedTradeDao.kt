package com.tradingapp.metatrader.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tradingapp.metatrader.data.local.database.entities.ClosedTradeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClosedTradeDao {

    @Query("SELECT * FROM closed_trades ORDER BY exitTimeEpochSec DESC")
    fun observeAll(): Flow<List<ClosedTradeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ClosedTradeEntity)

    @Query("DELETE FROM closed_trades")
    suspend fun clear()
}
