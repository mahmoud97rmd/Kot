package com.tradingapp.metatrader.data.local.drawing

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "drawings",
    indices = [
        Index(value = ["instrument", "timeframe"])
    ]
)
data class DrawingEntity(
    @PrimaryKey val id: String,
    val instrument: String,
    val timeframe: String,
    val type: String,
    val payloadJson: String,
    val updatedAtMs: Long
)
