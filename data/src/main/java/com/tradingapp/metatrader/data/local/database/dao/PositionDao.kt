package com.tradingapp.metatrader.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tradingapp.metatrader.data.local.database.entities.PositionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PositionDao {

    @Query("SELECT * FROM positions ORDER BY entryTimeEpochSec DESC")
    fun observeAll(): Flow<List<PositionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PositionEntity)

    @Query("DELETE FROM positions WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM positions")
    suspend fun clear()
}
