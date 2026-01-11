package com.tradingapp.metatrader.core.backtest

import com.tradingapp.metatrader.domain.backtest.BacktestStrategy
import com.tradingapp.metatrader.domain.models.backtest.BacktestCandle
import com.tradingapp.metatrader.domain.models.backtest.BacktestConfig
import com.tradingapp.metatrader.domain.models.backtest.BacktestOrder
import com.tradingapp.metatrader.domain.models.backtest.BacktestResult
import com.tradingapp.metatrader.domain.models.backtest.EquityPoint

class BacktestEngine(
    private val config: BacktestConfig
) {

    data class Progress(val index: Int, val total: Int)

    fun run(
        candles: List<BacktestCandle>,
        strategy: BacktestStrategy,
        onProgress: ((Progress) -> Unit)? = null
    ): BacktestResult {
        val account = BacktestVirtualAccount(config)
        val equityCurve = ArrayList<EquityPoint>(candles.size)
        val equityRaw = ArrayList<Double>(candles.size)

        val history = ArrayList<BacktestCandle>(minOf(5000, candles.size))

        for (i in candles.indices) {
            val c = candles[i]

            // account checks: SL/TP within candle
            account.onCandle(
                high = c.high,
                low = c.low,
                close = c.close,
                timeSec = c.timeSec
            )

            // candle is "closed" now -> feed strategy
            history.add(c)

            // Strategy may open new trade at close price (close-to-close mode)
            val signal = strategy.onCandleClosed(history)
            if (signal != null) {
                val entry = applyExecutionFriction(c.close, signal.side)
                account.execute(
                    BacktestOrder(
                        side = signal.side,
                        lots = signal.lots,
                        entryPrice = entry,
                        stopLoss = signal.stopLoss,
                        takeProfit = signal.takeProfit,
                        timeSec = c.timeSec
                    )
                )
            }

            // equity point using balance only (realized) + floating optional could be added later
            val eq = account.balance
            equityCurve.add(EquityPoint(timeSec = c.timeSec, equity = eq))
            equityRaw.add(eq)

            onProgress?.invoke(Progress(i + 1, candles.size))
        }

        // close all remaining at last close
        if (candles.isNotEmpty()) {
            val last = candles.last()
            account.closeAll(last.close, last.timeSec)
        }

        val trades = account.tradeHistory()
        val metrics = PerformanceAnalyzer.analyze(trades, equityRaw, config.initialBalance)

        return BacktestResult(
            config = config,
            trades = trades,
            equityCurve = equityCurve,
            metrics = metrics
        )
    }

    private fun applyExecutionFriction(closePrice: Double, side: com.tradingapp.metatrader.domain.models.backtest.BacktestSide): Double {
        val spread = config.spreadPoints * config.pointValue
        val slip = config.slippagePoints * config.pointValue

        return when (side) {
            com.tradingapp.metatrader.domain.models.backtest.BacktestSide.BUY -> closePrice + spread / 2.0 + slip
            com.tradingapp.metatrader.domain.models.backtest.BacktestSide.SELL -> closePrice - spread / 2.0 - slip
        }
    }
}
