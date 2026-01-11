package com.tradingapp.metatrader.app.features.tester.engine

import com.tradingapp.metatrader.app.core.candles.Candle
import com.tradingapp.metatrader.app.core.expert.dsl.ExpertDslParser
import com.tradingapp.metatrader.app.core.expert.runtime.ExpertRuntime
import com.tradingapp.metatrader.app.core.trading.sim.VirtualAccount
import com.tradingapp.metatrader.app.data.local.cache.CandleCacheRepository
import com.tradingapp.metatrader.app.features.tester.model.BacktestReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BacktestRunner @Inject constructor(
    private val cache: CandleCacheRepository
) {
    suspend fun run(
        symbol: String,
        timeframe: String,
        scriptText: String,
        renderCount: Int = 2000
    ): BacktestReport = withContext(Dispatchers.Default) {
        val candles: List<Candle> = withContext(Dispatchers.IO) {
            cache.loadRecentUnified(symbol, timeframe, renderCount)
        }

        if (candles.size < 50) {
            throw IllegalStateException("Not enough cached candles for backtest. Connect Live first to fill cache (need >= 50).")
        }

        val model = ExpertDslParser().parse(scriptText)

        val account = VirtualAccount(balance = 10_000.0)
        val runtime = ExpertRuntime(model, account)

        val equityCurve = ArrayList<Double>(candles.size)

        // Process candles
        for (c in candles) {
            runtime.onCandle(symbol, timeframe, c)
            equityCurve.add(account.equity(c.close))
        }

        // Close any remaining positions at last close to finalize PnL
        val last = candles.last()
        account.closeAll(last.close, last.timeSec)
        equityCurve.add(account.equity(last.close))

        val trades = account.history
        val netProfit = trades.sumOf { it.profit }
        val maxDd = PerformanceAnalyzer.maxDrawdown(equityCurve)
        val winRate = PerformanceAnalyzer.winRate(trades.map { it.profit })

        BacktestReport(
            symbol = symbol,
            timeframe = timeframe,
            candles = candles.size,
            trades = trades,
            netProfit = netProfit,
            maxDrawdown = maxDd,
            winRate = winRate,
            equityCurve = equityCurve
        )
    }
}
