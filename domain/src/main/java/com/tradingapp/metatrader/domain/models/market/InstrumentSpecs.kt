package com.tradingapp.metatrader.domain.models.market

object InstrumentSpecs {

    fun resolve(instrument: String): InstrumentSpec {
        return when (instrument.uppercase()) {
            "XAU_USD", "XAUUSD" -> InstrumentSpec(instrument = instrument, contractSize = 100.0, minLot = 0.01, lotStep = 0.01)
            "EUR_USD", "EURUSD",
            "GBP_USD", "GBPUSD",
            "USD_JPY", "USDJPY" -> InstrumentSpec(instrument = instrument, contractSize = 100_000.0, minLot = 0.01, lotStep = 0.01)
            else -> InstrumentSpec(instrument = instrument, contractSize = 1_000.0, minLot = 0.01, lotStep = 0.01)
        }
    }

    fun roundToStep(value: Double, step: Double): Double {
        if (step <= 0.0) return value
        val k = kotlin.math.floor(value / step)
        return k * step
    }
}
