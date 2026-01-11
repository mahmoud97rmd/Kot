package com.tradingapp.metatrader.app.features.expert.engine.backtest

import com.tradingapp.metatrader.app.features.expert.engine.runtime.ExpertRuntime
import com.tradingapp.metatrader.app.features.expert.engine.runtime.RhinoExpertRuntime
import com.tradingapp.metatrader.app.features.expert.engine.shared.BarSnapshot
import com.tradingapp.metatrader.app.features.expert.engine.shared.ExpertAction
import com.tradingapp.metatrader.app.features.expert.engine.shared.ExpertSafetyGate
import com.tradingapp.metatrader.domain.models.backtest.BacktestCandle
import com.tradingapp.metatrader.domain.models.backtest.BacktestConfig
import com.tradingapp.metatrader.domain.models.backtest.BacktestResult
import com.tradingapp.metatrader.domain.models.backtest.EquityPoint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpertBacktestRunner @Inject constructor() {

    data class Output(
        val result: BacktestResult,
        val logs: List<String>
    )

    fun run(
        candles: List<BacktestCandle>,
        expertCode: String,
        symbol: String,
        timeframe: String,
        config: BacktestConfig
    ): Output {
        val logs = ArrayList<String>()
        fun log(msg: String) { logs.add(msg) }

        val runtime: ExpertRuntime = RhinoExpertRuntime()
        runtime.init(expertCode = expertCode, expertName = "EA", symbol = symbol, timeframe = timeframe)

        val safety = ExpertSafetyGate(cooldownMs = 0, maxPositions = 1) // cooldown=0 in backtest (deterministic)
        val broker = BacktestBroker(
            pointValue = config.pointValue,
            commissionPerLot = config.commissionPerLot,
            spreadPoints = config.spreadPoints
        )

        var balance = config.initialBalance
        val equityCurve = ArrayList<EquityPoint>(candles.size)

        runtime.onInit().forEach { handleAction(it, broker, safety, symbol, timeframe, log, config) }

        for (c in candles) {
            val barOpenSec = c.timeSec
            safety.onNewBar(barOpenSec)
            safety.setOpenPositions(broker.openPositionsCount())

            // broker checks SL/TP on each bar
            broker.onBar(timeSec = c.timeSec, openPrice = c.open, high = c.high, low = c.low, close = c.close)

            // update openPositions after possible close
            safety.setOpenPositions(broker.openPositionsCount())

            val bar = BarSnapshot(
                symbol = symbol,
                timeframe = timeframe,
                openTimeSec = c.timeSec,
                open = c.open,
                high = c.high,
                low = c.low,
                close = c.close
            )

            val acts = runtime.onBar(bar)
            for (a in acts) {
                handleAction(a, broker, safety, symbol, timeframe, log, config, barClose = c.close, timeSec = c.timeSec)
            }

            // apply closed trades PnL to balance at their close time
            // (simple: recompute using broker trades diff)
            val trades = broker.getTrades()
            var pnlSum = 0.0
            for (t in trades) pnlSum += t.profit
            balance = config.initialBalance + pnlSum

            equityCurve.add(EquityPoint(timeSec = c.timeSec, equity = balance))
        }

        runtime.close()

        val trades = broker.getTrades()
        val net = trades.sumOf { it.profit }
        val wins = trades.count { it.profit > 0.0 }
        val total = trades.size
        val winRate = if (total == 0) 0.0 else (wins.toDouble() / total.toDouble())

        // max drawdown (simple)
        var peak = config.initialBalance
        var maxDd = 0.0
        for (p in equityCurve) {
            if (p.equity > peak) peak = p.equity
            val dd = peak - p.equity
            if (dd > maxDd) maxDd = dd
        }

        val res = BacktestResult(
            config = config,
            totalTrades = total,
            winRate = winRate,
            netProfit = net,
            maxDrawdown = maxDd,
            trades = trades,
            equityCurve = equityCurve
        )

        return Output(result = res, logs = logs)
    }

    private fun handleAction(
        a: ExpertAction,
        broker: BacktestBroker,
        safety: ExpertSafetyGate,
        symbol: String,
        timeframe: String,
        log: (String) -> Unit,
        config: BacktestConfig,
        barClose: Double = 0.0,
        timeSec: Long = 0L
    ) {
        when (a) {
            is ExpertAction.Log -> log("[EA] ${a.level}: ${a.message}")

            is ExpertAction.MarketBuy -> {
                val now = System.currentTimeMillis()
                safety.setOpenPositions(broker.openPositionsCount())
                if (!safety.canPlaceOrder(now)) {
                    log("[EA] BLOCK BUY (safety)")
                    return
                }
                val ok = broker.placeMarket(
                    side = com.tradingapp.metatrader.app.core.trading.OrderSide.BUY,
                    units = a.units,
                    timeSec = timeSec,
                    price = barClose,
                    tp = a.tp,
                    sl = a.sl
                )
                if (ok) {
                    safety.markOrderPlaced(now)
                    log("[EA] BUY units=${a.units} tp=${a.tp} sl=${a.sl}")
                } else {
                    log("[EA] BUY rejected (position exists)")
                }
            }

            is ExpertAction.MarketSell -> {
                val now = System.currentTimeMillis()
                safety.setOpenPositions(broker.openPositionsCount())
                if (!safety.canPlaceOrder(now)) {
                    log("[EA] BLOCK SELL (safety)")
                    return
                }
                val ok = broker.placeMarket(
                    side = com.tradingapp.metatrader.app.core.trading.OrderSide.SELL,
                    units = a.units,
                    timeSec = timeSec,
                    price = barClose,
                    tp = a.tp,
                    sl = a.sl
                )
                if (ok) {
                    safety.markOrderPlaced(now)
                    log("[EA] SELL units=${a.units} tp=${a.tp} sl=${a.sl}")
                } else {
                    log("[EA] SELL rejected (position exists)")
                }
            }

            ExpertAction.CloseAll -> {
                broker.closeAll(timeSec = timeSec, price = barClose)
                log("[EA] CloseAll executed")
            }
        }
    }
}
