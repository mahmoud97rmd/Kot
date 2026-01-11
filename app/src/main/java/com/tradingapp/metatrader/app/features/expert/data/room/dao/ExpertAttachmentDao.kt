package com.tradingapp.metatrader.app.features.expert.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tradingapp.metatrader.app.features.expert.data.room.entities.ExpertAttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpertAttachmentDao {

    @Query("SELECT * FROM expert_attachments ORDER BY updatedAtMs DESC")
    fun observeAll(): Flow<List<ExpertAttachmentEntity>>

    @Query("SELECT * FROM expert_attachments WHERE symbol = :symbol AND timeframe = :timeframe LIMIT 1")
    suspend fun getBySymbolTf(symbol: String, timeframe: String): ExpertAttachmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ExpertAttachmentEntity)

    @Query("DELETE FROM expert_attachments WHERE symbol = :symbol AND timeframe = :timeframe")
    suspend fun deleteBySymbolTf(symbol: String, timeframe: String)

    @Query("DELETE FROM expert_attachments WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE expert_attachments SET isActive = :active, updatedAtMs = :updatedAtMs WHERE id = :id")
    suspend fun setActive(id: String, active: Boolean, updatedAtMs: Long)
}
