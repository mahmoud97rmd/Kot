package com.tradingapp.metatrader.domain.models.backtest

data class BacktestCandle(
    val timeSec: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long = 0L
)
