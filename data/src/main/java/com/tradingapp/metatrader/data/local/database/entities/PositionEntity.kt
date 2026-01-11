package com.tradingapp.metatrader.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "positions")
data class PositionEntity(
    @PrimaryKey val id: String,
    val instrument: String,
    val side: String,
    val entryTimeEpochSec: Long,
    val entryPrice: Double,
    val lots: Double,
    val stopLoss: Double?,
    val takeProfit: Double?,
    val comment: String?
)
