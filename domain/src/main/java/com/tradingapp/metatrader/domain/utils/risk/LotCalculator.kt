package com.tradingapp.metatrader.domain.utils.risk

import com.tradingapp.metatrader.domain.models.market.InstrumentSpecs
import kotlin.math.abs
import kotlin.math.max

object LotCalculator {

    /**
     * Educational approximation:
     * riskMoney = balance * (riskPercent/100)
     * perLotLoss = abs(entry - sl) * contractSize
     * lots = riskMoney / perLotLoss
     */
    fun calcLots(
        instrument: String,
        balance: Double,
        riskPercent: Double,
        entryPrice: Double,
        stopLossPrice: Double
    ): Double {
        val spec = InstrumentSpecs.resolve(instrument)
        val riskMoney = max(0.0, balance) * (max(0.0, riskPercent) / 100.0)
        val distance = abs(entryPrice - stopLossPrice)
        if (distance <= 0.0) return spec.minLot

        val perLotLoss = distance * spec.contractSize
        if (perLotLoss <= 0.0) return spec.minLot

        val rawLots = riskMoney / perLotLoss
        val stepped = InstrumentSpecs.roundToStep(rawLots, spec.lotStep)
        return max(spec.minLot, stepped)
    }
}
