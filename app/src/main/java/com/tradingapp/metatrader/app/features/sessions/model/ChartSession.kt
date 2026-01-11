package com.tradingapp.metatrader.app.features.sessions.model

data class ChartSession(
    val id: String,
    val symbol: String,
    val timeframe: String,
    val title: String,
    val createdAtMs: Long,
    val lastUsedAtMs: Long
)
