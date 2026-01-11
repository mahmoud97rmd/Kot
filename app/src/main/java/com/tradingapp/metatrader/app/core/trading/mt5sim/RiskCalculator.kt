package com.tradingapp.metatrader.app.core.trading.mt5sim

object RiskCalculator {

    /**
     * Calculates lots so that loss at SL equals riskPercent of balance.
     *
     * riskPercent: e.g. 1.0 for 1%
     * slPips: distance from entry to SL in pips
     */
    fun lotsForRisk(balance: Double, riskPercent: Double, spec: InstrumentSpec, slPips: Double): Double {
        if (balance <= 0.0) return 0.0
        if (riskPercent <= 0.0) return 0.0
        if (slPips <= 0.0) return 0.0

        val riskMoney = balance * (riskPercent / 100.0)
        val slDistancePrice = slPips * spec.pipSize
        val lossPerLot = slDistancePrice * spec.contractMultiplier
        if (lossPerLot <= 0.0) return 0.0

        val lots = riskMoney / lossPerLot
        // clamp to reasonable range
        return lots.coerceIn(0.01, 100.0)
    }
}
