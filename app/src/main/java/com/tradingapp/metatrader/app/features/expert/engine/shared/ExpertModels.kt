package com.tradingapp.metatrader.app.features.expert.engine.shared

data class TickSnapshot(
    val symbol: String,
    val timeEpochMs: Long,
    val bid: Double,
    val ask: Double
) {
    val mid: Double get() = (bid + ask) / 2.0
}

data class BarSnapshot(
    val symbol: String,
    val timeframe: String,
    val openTimeSec: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double
)

sealed class ExpertAction {
    data class Log(val level: String, val message: String) : ExpertAction()
    data class MarketBuy(val units: Long, val tp: Double? = null, val sl: Double? = null) : ExpertAction()
    data class MarketSell(val units: Long, val tp: Double? = null, val sl: Double? = null) : ExpertAction()
    object CloseAll : ExpertAction()
}
