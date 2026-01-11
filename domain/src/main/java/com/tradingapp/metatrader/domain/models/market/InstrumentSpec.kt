package com.tradingapp.metatrader.domain.models.market

data class InstrumentSpec(
    val instrument: String,
    val contractSize: Double, // e.g., Forex 100000, XAU 100 (oz)
    val minLot: Double = 0.01,
    val lotStep: Double = 0.01
)
