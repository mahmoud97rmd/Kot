package com.tradingapp.metatrader.app.core.expert.supervisor

data class AttachedExpert(
    val scriptId: Long,
    val scriptName: String,
    val scriptText: String,
    val symbol: String,
    val timeframe: String
)
