package com.tradingapp.metatrader.app.features.backtest.ui.journal

data class JournalEntry(
    val timeSec: Long,
    val title: String,
    val details: String
)
