package com.tradingapp.metatrader.app.features.expert.runtime.logs

data class ExpertLogLine(
    val timeMs: Long,
    val level: String,
    val message: String
)
