package com.tradingapp.metatrader

import com.tradingapp.metatrader.app.core.trading.mt5sim.InstrumentCatalog
import com.tradingapp.metatrader.app.core.trading.mt5sim.PriceQuote
import com.tradingapp.metatrader.app.core.trading.mt5sim.Side
import com.tradingapp.metatrader.app.core.trading.mt5sim.VirtualAccountMt5
import org.junit.Assert.*
import org.junit.Test

class AccountMt5Test {

    @Test
    fun open_and_close_profit_buy() {
        val acc = VirtualAccountMt5(balance = 10000.0)
        val spec = InstrumentCatalog.spec("EUR_USD")
        val qOpen = PriceQuote(timeSec = 1, bid = 1.1000, ask = 1.1002)
        acc.openMarket(spec, "EUR_USD", Side.BUY, lots = 1.0, quote = qOpen)

        val qClose = PriceQuote(timeSec = 2, bid = 1.1010, ask = 1.1012)
        val posId = acc.positions.first().id
        val deal = acc.closePartial(spec, posId, 1.0, qClose)!!
        assertTrue(deal.profit > 0.0)
        assertTrue(acc.positions.isEmpty())
    }

    @Test
    fun stop_loss_hits_on_candle_low() {
        val acc = VirtualAccountMt5(balance = 10000.0)
        val spec = InstrumentCatalog.spec("EUR_USD")
        val qOpen = PriceQuote(timeSec = 1, bid = 1.1000, ask = 1.1002)
        val pos = acc.openMarket(spec, "EUR_USD", Side.BUY, lots = 1.0, quote = qOpen, sl = 1.0990, tp = null)

        val deals = acc.checkStopsOnCandle(
            spec = spec,
            candleTimeSec = 60,
            candleHigh = 1.1010,
            candleLow = 1.0985,
            candleClose = 1.1000,
            conservativeWorstCase = true
        )
        assertTrue(deals.isNotEmpty())
        assertTrue(acc.positions.none { it.id == pos.id })
    }
}
