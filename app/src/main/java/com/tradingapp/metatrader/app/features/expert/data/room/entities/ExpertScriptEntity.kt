package com.tradingapp.metatrader.app.features.expert.data.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expert_scripts")
data class ExpertScriptEntity(
    @PrimaryKey val id: String,
    val name: String,
    val language: String,
    val code: String,
    val createdAtMs: Long,
    val updatedAtMs: Long,
    val isEnabled: Boolean
)
