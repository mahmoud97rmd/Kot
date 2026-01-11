package com.tradingapp.metatrader.data.local.backtestdb.entities

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "candles",
    primaryKeys = ["instrument", "granularity", "timeSec"],
    indices = [
        Index(value = ["instrument", "granularity", "timeSec"])
    ]
)
data class CandleEntity(
    val instrument: String,     // e.g. XAU_USD
    val granularity: String,    // e.g. M1, M5, M15, H1
    val timeSec: Long,          // candle open time UTC epoch seconds
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long? = null
)
