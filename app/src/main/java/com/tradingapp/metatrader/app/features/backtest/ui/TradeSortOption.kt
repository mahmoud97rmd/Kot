package com.tradingapp.metatrader.app.features.backtest.ui

enum class TradeSortOption(val label: String) {
    TIME_ASC("Time ↑"),
    TIME_DESC("Time ↓"),
    PROFIT_DESC("Profit ↓"),
    PROFIT_ASC("Profit ↑");

    companion object {
        fun fromPosition(pos: Int): TradeSortOption {
            return values().getOrElse(pos) { TIME_DESC }
        }
    }
}
