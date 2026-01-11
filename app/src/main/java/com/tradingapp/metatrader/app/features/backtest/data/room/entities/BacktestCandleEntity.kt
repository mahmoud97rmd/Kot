package com.tradingapp.metatrader.app.features.backtest.data.room.entities

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "backtest_candles",
    primaryKeys = ["instrument", "granularity", "timeSec"],
    indices = [
        Index(value = ["instrument", "granularity", "timeSec"])
    ]
)
data class BacktestCandleEntity(
    val instrument: String,
    val granularity: String,
    val timeSec: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double
)
