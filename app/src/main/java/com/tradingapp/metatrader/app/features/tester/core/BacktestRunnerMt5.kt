package com.tradingapp.metatrader.app.features.tester.core

import com.tradingapp.metatrader.app.core.candles.Candle
import com.tradingapp.metatrader.app.core.expert.runtime.ExpertRuntimeMt5
import com.tradingapp.metatrader.app.core.trading.mt5sim.DealMt5
import com.tradingapp.metatrader.app.core.trading.mt5sim.InstrumentCatalog
import com.tradingapp.metatrader.app.core.trading.mt5sim.PriceQuote
import com.tradingapp.metatrader.app.core.trading.mt5sim.VirtualAccountMt5

data class BacktestResultMt5(
    val startBalance: Double,
    val endBalance: Double,
    val deals: List<DealMt5>
)

class BacktestRunnerMt5 {

    fun run(symbol: String, timeframe: String, scriptText: String, candles: List<Candle>): BacktestResultMt5 {
        val spec = InstrumentCatalog.spec(symbol)
        val account = VirtualAccountMt5(balance = 10_000.0)
        val runtime = ExpertRuntimeMt5(scriptText, account)

        for (c in candles) {
            val q = PriceQuote(timeSec = c.timeSec, bid = c.close, ask = c.close)
            account.checkPendingOnQuote(spec, q)
            runtime.onClosedBar(symbol, timeframe, c)
            account.checkStopsOnCandle(
                spec = spec,
                candleTimeSec = c.timeSec,
                candleHigh = c.high,
                candleLow = c.low,
                candleClose = c.close,
                conservativeWorstCase = true
            )
        }

        return BacktestResultMt5(
            startBalance = 10_000.0,
            endBalance = account.balance,
            deals = account.history.toList()
        )
    }
}
