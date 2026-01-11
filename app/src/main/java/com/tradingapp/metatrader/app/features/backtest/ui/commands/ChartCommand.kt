package com.tradingapp.metatrader.app.features.backtest.ui.commands

sealed class ChartCommand {
    data class JumpToTime(val timeSec: Long) : ChartCommand()
    data class JumpToTradeEntry(val tradeId: String) : ChartCommand()
    data class JumpToTradeExit(val tradeId: String) : ChartCommand()
}
