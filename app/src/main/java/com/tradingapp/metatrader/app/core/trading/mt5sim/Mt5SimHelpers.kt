package com.tradingapp.metatrader.app.core.trading.mt5sim

import com.tradingapp.metatrader.app.core.candles.Candle

object Mt5SimHelpers {

    fun quoteFromCandleClose(spec: InstrumentSpec, c: Candle, spreadPips: Double = spec.defaultSpreadPips): PriceQuote {
        return VirtualAccountMt5.quoteFromMidWithSpread(spec, c.timeSec, c.close, spreadPips)
    }

    fun slForMarket(spec: InstrumentSpec, side: Side, entryPrice: Double, slPips: Double?): Double? {
        val p = slPips ?: return null
        if (p <= 0.0) return null
        val d = p * spec.pipSize
        return if (side == Side.BUY) (entryPrice - d) else (entryPrice + d)
    }

    fun tpForMarket(spec: InstrumentSpec, side: Side, entryPrice: Double, tpPips: Double?): Double? {
        val p = tpPips ?: return null
        if (p <= 0.0) return null
        val d = p * spec.pipSize
        return if (side == Side.BUY) (entryPrice + d) else (entryPrice - d)
    }
}
