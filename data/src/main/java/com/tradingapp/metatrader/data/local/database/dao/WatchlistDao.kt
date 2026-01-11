package com.tradingapp.metatrader.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tradingapp.metatrader.data.local.database.entities.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    @Query("SELECT * FROM watchlist ORDER BY displayName ASC")
    fun observeAll(): Flow<List<WatchlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE instrument = :instrument")
    suspend fun delete(instrument: String)
}
