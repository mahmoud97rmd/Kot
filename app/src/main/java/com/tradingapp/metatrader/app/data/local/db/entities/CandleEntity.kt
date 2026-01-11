package com.tradingapp.metatrader.app.data.local.db.entities

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "candles",
    primaryKeys = ["symbol", "timeframe", "timeSec"],
    indices = [
        Index(value = ["symbol", "timeframe", "timeSec"])
    ]
)
data class CandleEntity(
    val symbol: String,
    val timeframe: String,
    val timeSec: Long,

    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,

    val volume: Long
)
