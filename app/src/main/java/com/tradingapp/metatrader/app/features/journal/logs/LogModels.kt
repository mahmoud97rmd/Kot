package com.tradingapp.metatrader.app.features.journal.logs

enum class LogLevel { INFO, WARN, ERROR }

data class LogEntry(
    val timeMs: Long,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val symbol: String? = null,
    val timeframe: String? = null,
    val expertName: String? = null
)
