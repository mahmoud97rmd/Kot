package com.tradingapp.metatrader.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tradingapp.metatrader.data.local.database.entities.PendingOrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingOrderDao {

    @Query("SELECT * FROM pending_orders ORDER BY createdAtEpochSec DESC")
    fun observeAll(): Flow<List<PendingOrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PendingOrderEntity)

    @Query("DELETE FROM pending_orders WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM pending_orders")
    suspend fun clear()
}
