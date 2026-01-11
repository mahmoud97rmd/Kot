package com.tradingapp.metatrader.app.features.tester.model

import com.tradingapp.metatrader.app.core.trading.sim.ClosedTrade

data class BacktestReport(
    val symbol: String,
    val timeframe: String,
    val candles: Int,
    val trades: List<ClosedTrade>,
    val netProfit: Double,
    val maxDrawdown: Double,
    val winRate: Double,
    val equityCurve: List<Double>
)
