package com.tradingapp.metatrader.data.local.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "closed_trades",
    indices = [Index(value = ["exitTimeEpochSec"])]
)
data class ClosedTradeEntity(
    @PrimaryKey val id: String,
    val instrument: String,
    val side: String,
    val entryTimeEpochSec: Long,
    val exitTimeEpochSec: Long,
    val entryPrice: Double,
    val exitPrice: Double,
    val lots: Double,
    val profit: Double,
    val comment: String?
)
