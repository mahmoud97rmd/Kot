package com.tradingapp.metatrader.app.features.chart.indicators

data class IndicatorConfig(
    val emaPeriods: List<Int> = listOf(20, 50),
    val stochK: Int = 14,
    val stochD: Int = 3
)
