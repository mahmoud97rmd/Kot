package com.tradingapp.metatrader.app.features.expert.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tradingapp.metatrader.app.features.expert.data.room.entities.ExpertScriptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpertScriptDao {

    @Query("SELECT * FROM expert_scripts ORDER BY updatedAtMs DESC")
    fun observeAll(): Flow<List<ExpertScriptEntity>>

    @Query("SELECT * FROM expert_scripts WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ExpertScriptEntity?

    @Query("SELECT * FROM expert_scripts WHERE isEnabled = 1 LIMIT 1")
    suspend fun getEnabled(): ExpertScriptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ExpertScriptEntity)

    @Query("DELETE FROM expert_scripts WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE expert_scripts SET isEnabled = 0")
    suspend fun disableAll()

    @Query("UPDATE expert_scripts SET isEnabled = 1 WHERE id = :id")
    suspend fun enable(id: String)
}
