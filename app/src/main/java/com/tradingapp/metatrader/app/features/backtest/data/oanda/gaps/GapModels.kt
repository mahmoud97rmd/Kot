package com.tradingapp.metatrader.app.features.backtest.data.oanda.gaps

data class TimeGap(val fromSec: Long, val toSec: Long) {
    init {
        require(fromSec <= toSec) { "fromSec must be <= toSec" }
    }
}
