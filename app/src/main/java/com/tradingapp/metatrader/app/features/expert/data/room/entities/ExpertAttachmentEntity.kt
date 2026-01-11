package com.tradingapp.metatrader.app.features.expert.data.room.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expert_attachments",
    indices = [
        Index(value = ["symbol", "timeframe"], unique = true),
        Index(value = ["scriptId"])
    ]
)
data class ExpertAttachmentEntity(
    @PrimaryKey val id: String,
    val scriptId: String,
    val symbol: String,
    val timeframe: String,
    val isActive: Boolean,
    val createdAtMs: Long,
    val updatedAtMs: Long
)
