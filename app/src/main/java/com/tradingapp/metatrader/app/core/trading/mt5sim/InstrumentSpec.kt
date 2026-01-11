package com.tradingapp.metatrader.app.core.trading.mt5sim

/**
 * Minimal instrument spec needed for simulation.
 *
 * pipSize:
 * - For many FX pairs: 0.0001 (or 0.01 for JPY)
 * - For XAUUSD in many brokers: 0.01 (varies)
 *
 * contractMultiplier:
 * - Used to transform price diff into profit units for 1 lot.
 */
data class InstrumentSpec(
    val symbol: String,
    val pipSize: Double,
    val contractMultiplier: Double,
    val defaultSpreadPips: Double = 20.0,
    val commissionPerLot: Double = 0.0
)

object InstrumentCatalog {
    /**
     * Provide sane defaults; you can tune per broker later.
     */
    fun spec(symbol: String): InstrumentSpec {
        return when (symbol.uppercase()) {
            "XAU_USD", "XAUUSD" -> InstrumentSpec(symbol, pipSize = 0.01, contractMultiplier = 100.0, defaultSpreadPips = 30.0)
            "EUR_USD", "EURUSD" -> InstrumentSpec(symbol, pipSize = 0.0001, contractMultiplier = 100000.0, defaultSpreadPips = 10.0)
            "GBP_USD", "GBPUSD" -> InstrumentSpec(symbol, pipSize = 0.0001, contractMultiplier = 100000.0, defaultSpreadPips = 12.0)
            "USD_JPY", "USDJPY" -> InstrumentSpec(symbol, pipSize = 0.01, contractMultiplier = 100000.0, defaultSpreadPips = 10.0)
            else -> InstrumentSpec(symbol, pipSize = 0.0001, contractMultiplier = 100000.0, defaultSpreadPips = 12.0)
        }
    }
}
