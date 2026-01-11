package com.tradingapp.metatrader.data.local.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "candles",
    indices = [Index(value = ["instrument", "timeframe", "timeEpochSec"], unique = true)]
)
data class CandleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val instrument: String,
    val timeframe: String,
    val timeEpochSec: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)
