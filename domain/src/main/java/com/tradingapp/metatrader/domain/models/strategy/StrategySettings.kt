package com.tradingapp.metatrader.domain.models.strategy

data class StrategySettings(
    val riskPercent: Double = 1.0,
    val atrPeriod: Int = 14,
    val slAtrMult: Double = 1.5,
    val tpAtrMult: Double = 2.0,
    val emaFast: Int = 50,
    val emaSlow: Int = 150,
    val stochPeriod: Int = 14,
    val stochTrigger: Double = 20.0
)
