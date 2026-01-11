package com.tradingapp.metatrader.domain.backtest

import com.tradingapp.metatrader.domain.models.backtest.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

class BacktestEngine {

    data class Progress(val index: Int, val total: Int)

    fun run(
        candles: List<BacktestCandle>,
        strategy: BacktestStrategy,
        config: BacktestConfig,
        onProgress: ((Progress) -> Unit)? = null
    ): BacktestResult {
        require(candles.isNotEmpty()) { "Candles cannot be empty" }

        val trades = ArrayList<BacktestTrade>()
        val equityCurve = ArrayList<EquityPoint>(candles.size)

        var balance = config.initialBalance
        var equity = balance

        var openPos: OpenPosition? = null
        var maxEquityPeak = equity
        var maxDrawdown = 0.0

        val history = ArrayList<BacktestCandle>(candles.size)

        var pendingSignal: BacktestSignal? = null
        var pendingSignalTimeSec: Long? = null

        val total = candles.size
        for (i in candles.indices) {
            val c = candles[i]
            history.add(c)

            onProgress?.invoke(Progress(i + 1, total))

            // 1) Execute pending signal at OPEN (OPEN_PRICES_ONLY)
            if (config.modelingMode == ModelingMode.OPEN_PRICES_ONLY && pendingSignal != null && openPos == null) {
                val sig = pendingSignal!!
                val lots = resolveLots(balance, sig.lots, config)
                val entry = applyCosts(price = c.open, side = sig.side, cfg = config)

                openPos = OpenPosition(
                    side = sig.side,
                    lots = lots,
                    entryPrice = entry,
                    entryTimeSec = pendingSignalTimeSec ?: c.timeSec,
                    stopLoss = calcStopLoss(entry, sig.side, config),
                    takeProfit = calcTakeProfit(entry, sig.side, config)
                )

                pendingSignal = null
                pendingSignalTimeSec = null
            }

            // 2) Manage open position (SL/TP / forced close)
            if (openPos != null) {
                val exit = when (config.modelingMode) {
                    ModelingMode.OPEN_PRICES_ONLY -> checkExitAtOpenOnly(openPos!!, c, config)
                    ModelingMode.CANDLE_EXTREMES -> checkExitWithinCandle(openPos!!, c, config)
                }

                if (exit != null) {
                    val (exitPrice, exitTime) = exit
                    val profit = calcProfit(openPos!!, exitPrice, config)
                    balance += profit
                    equity = balance

                    trades.add(
                        BacktestTrade(
                            id = "T${trades.size + 1}",
                            side = openPos!!.side,
                            lots = openPos!!.lots,
                            entryTimeSec = openPos!!.entryTimeSec,
                            entryPrice = openPos!!.entryPrice,
                            exitTimeSec = exitTime,
                            exitPrice = exitPrice,
                            profit = profit,
                            stopLoss = openPos!!.stopLoss,
                            takeProfit = openPos!!.takeProfit
                        )
                    )
                    openPos = null
                } else {
                    equity = balance + calcProfit(openPos!!, c.close, config)
                }
            }

            // 3) Strategy signal on candle close
            val signal = strategy.onCandleClosed(history)

            if (signal != null) {
                when (config.modelingMode) {
                    ModelingMode.CANDLE_EXTREMES -> {
                        if (openPos == null) {
                            val lots = resolveLots(balance, signal.lots, config)
                            val entry = applyCosts(price = c.close, side = signal.side, cfg = config)

                            openPos = OpenPosition(
                                side = signal.side,
                                lots = lots,
                                entryPrice = entry,
                                entryTimeSec = c.timeSec,
                                stopLoss = calcStopLoss(entry, signal.side, config),
                                takeProfit = calcTakeProfit(entry, signal.side, config)
                            )
                        } else {
                            // opposite signal closes/reverses at close (simple)
                            if (openPos!!.side != signal.side) {
                                val exitPrice = applyCosts(price = c.close, side = opposite(openPos!!.side), cfg = config)
                                val profit = calcProfit(openPos!!, exitPrice, config)
                                balance += profit
                                equity = balance

                                trades.add(
                                    BacktestTrade(
                                        id = "T${trades.size + 1}",
                                        side = openPos!!.side,
                                        lots = openPos!!.lots,
                                        entryTimeSec = openPos!!.entryTimeSec,
                                        entryPrice = openPos!!.entryPrice,
                                        exitTimeSec = c.timeSec,
                                        exitPrice = exitPrice,
                                        profit = profit,
                                        stopLoss = openPos!!.stopLoss,
                                        takeProfit = openPos!!.takeProfit
                                    )
                                )
                                openPos = null
                            }
                        }
                    }

                    ModelingMode.OPEN_PRICES_ONLY -> {
                        if (openPos == null) {
                            if (i < candles.lastIndex) {
                                pendingSignal = signal
                                pendingSignalTimeSec = c.timeSec
                            }
                        } else {
                            if (openPos!!.side != signal.side && i < candles.lastIndex) {
                                openPos = openPos!!.copy(forceCloseNextOpen = true)
                                pendingSignal = signal
                                pendingSignalTimeSec = c.timeSec
                            }
                        }
                    }
                }
            }

            // 4) equity curve
            maxEquityPeak = max(maxEquityPeak, equity)
            val dd = maxEquityPeak - equity
            if (dd > maxDrawdown) maxDrawdown = dd
            equityCurve.add(EquityPoint(timeSec = c.timeSec, equity = equity))
        }

        // close open position at last close
        if (openPos != null) {
            val last = candles.last()
            val exitPrice = applyCosts(price = last.close, side = opposite(openPos!!.side), cfg = config)
            val profit = calcProfit(openPos!!, exitPrice, config)
            balance += profit
            equity = balance

            trades.add(
                BacktestTrade(
                    id = "T${trades.size + 1}",
                    side = openPos!!.side,
                    lots = openPos!!.lots,
                    entryTimeSec = openPos!!.entryTimeSec,
                    entryPrice = openPos!!.entryPrice,
                    exitTimeSec = last.timeSec,
                    exitPrice = exitPrice,
                    profit = profit,
                    stopLoss = openPos!!.stopLoss,
                    takeProfit = openPos!!.takeProfit
                )
            )
            openPos = null
        }

        val metrics = MetricsCalculator.calculate(trades, maxDrawdown, equityCurve, config.initialBalance)

        return BacktestResult(
            config = config,
            trades = trades,
            equityCurve = equityCurve,
            metrics = metrics
        )
    }

    private fun resolveLots(balance: Double, requestedLots: Double, cfg: BacktestConfig): Double {
        val riskPct = cfg.riskPercent
        val slPts = cfg.stopLossPoints
        if (riskPct > 0.0 && slPts > 0.0) {
            val riskMoney = balance * (riskPct / 100.0)
            val slDistance = slPts * cfg.pointValue
            if (slDistance > 0.0) {
                val lots = riskMoney / slDistance
                return lots.coerceIn(0.01, 100.0)
            }
        }
        return requestedLots.coerceIn(0.01, 100.0)
    }

    private fun calcStopLoss(entry: Double, side: BacktestSide, cfg: BacktestConfig): Double? {
        val pts = cfg.stopLossPoints
        if (pts <= 0.0) return null
        val d = pts * cfg.pointValue
        return if (side == BacktestSide.BUY) entry - d else entry + d
    }

    private fun calcTakeProfit(entry: Double, side: BacktestSide, cfg: BacktestConfig): Double? {
        val pts = cfg.takeProfitPoints
        if (pts <= 0.0) return null
        val d = pts * cfg.pointValue
        return if (side == BacktestSide.BUY) entry + d else entry - d
    }

    private fun checkExitAtOpenOnly(pos: OpenPosition, c: BacktestCandle, cfg: BacktestConfig): Pair<Double, Long>? {
        if (pos.forceCloseNextOpen) {
            val exit = applyCosts(price = c.open, side = opposite(pos.side), cfg = cfg)
            return Pair(exit, c.timeSec)
        }

        val open = c.open
        pos.stopLoss?.let { sl ->
            if (pos.side == BacktestSide.BUY && open <= sl) return Pair(applyCosts(sl, opposite(pos.side), cfg), c.timeSec)
            if (pos.side == BacktestSide.SELL && open >= sl) return Pair(applyCosts(sl, opposite(pos.side), cfg), c.timeSec)
        }
        pos.takeProfit?.let { tp ->
            if (pos.side == BacktestSide.BUY && open >= tp) return Pair(applyCosts(tp, opposite(pos.side), cfg), c.timeSec)
            if (pos.side == BacktestSide.SELL && open <= tp) return Pair(applyCosts(tp, opposite(pos.side), cfg), c.timeSec)
        }
        return null
    }

    private fun checkExitWithinCandle(pos: OpenPosition, c: BacktestCandle, cfg: BacktestConfig): Pair<Double, Long>? {
        val sl = pos.stopLoss
        val tp = pos.takeProfit

        // conservative: SL first if both touched
        if (pos.side == BacktestSide.BUY) {
            val slHit = sl != null && c.low <= sl
            val tpHit = tp != null && c.high >= tp
            return when {
                slHit -> Pair(applyCosts(sl!!, opposite(pos.side), cfg), c.timeSec)
                tpHit -> Pair(applyCosts(tp!!, opposite(pos.side), cfg), c.timeSec)
                else -> null
            }
        } else {
            val slHit = sl != null && c.high >= sl
            val tpHit = tp != null && c.low <= tp
            return when {
                slHit -> Pair(applyCosts(sl!!, opposite(pos.side), cfg), c.timeSec)
                tpHit -> Pair(applyCosts(tp!!, opposite(pos.side), cfg), c.timeSec)
                else -> null
            }
        }
    }

    private fun opposite(side: BacktestSide): BacktestSide =
        if (side == BacktestSide.BUY) BacktestSide.SELL else BacktestSide.BUY

    private fun applyCosts(price: Double, side: BacktestSide, cfg: BacktestConfig): Double {
        val spread = cfg.spreadPoints * cfg.pointValue
        val slip = cfg.slippagePoints * cfg.pointValue

        val spreadAdj = if (side == BacktestSide.BUY) (spread / 2.0) else -(spread / 2.0)
        val slipAdj = if (side == BacktestSide.BUY) slip else -slip

        return price + spreadAdj + slipAdj
    }

    private fun calcProfit(pos: OpenPosition, exitPrice: Double, cfg: BacktestConfig): Double {
        val direction = if (pos.side == BacktestSide.BUY) 1.0 else -1.0
        val raw = (exitPrice - pos.entryPrice) * direction
        val commission = cfg.commissionPerLot * pos.lots
        return (raw * pos.lots) - commission
    }

    private data class OpenPosition(
        val side: BacktestSide,
        val lots: Double,
        val entryPrice: Double,
        val entryTimeSec: Long,
        val stopLoss: Double? = null,
        val takeProfit: Double? = null,
        val forceCloseNextOpen: Boolean = false
    )
}

private object MetricsCalculator {
    fun calculate(
        trades: List<BacktestTrade>,
        maxDrawdown: Double,
        equityCurve: List<EquityPoint>,
        initialBalance: Double
    ): PerformanceMetrics {

        val total = trades.size
        val grossProfit = trades.filter { it.profit > 0.0 }.sumOf { it.profit }
        val grossLossAbs = trades.filter { it.profit < 0.0 }.sumOf { abs(it.profit) }
        val net = trades.sumOf { it.profit }
        val wins = trades.count { it.profit > 0.0 }
        val winRate = if (total == 0) 0.0 else wins.toDouble() / total.toDouble()
        val pf = if (grossLossAbs <= 0.0) {
            if (grossProfit > 0.0) Double.POSITIVE_INFINITY else 0.0
        } else grossProfit / grossLossAbs

        val expectedPayoff = if (total == 0) 0.0 else (net / total.toDouble())
        val recoveryFactor = if (maxDrawdown <= 0.0) {
            if (net > 0.0) Double.POSITIVE_INFINITY else 0.0
        } else net / maxDrawdown

        val sharpeLike = sharpeLikeFromEquity(equityCurve, initialBalance)

        return PerformanceMetrics(
            netProfit = net,
            grossProfit = grossProfit,
            grossLoss = grossLossAbs,
            winRate = winRate,
            totalTrades = total,
            maxDrawdown = maxDrawdown,
            profitFactor = pf,
            expectedPayoff = expectedPayoff,
            recoveryFactor = recoveryFactor,
            sharpeLike = sharpeLike
        )
    }

    // Sharpe-like: mean(return)/std(return) using simple returns from equity points.
    private fun sharpeLikeFromEquity(equity: List<EquityPoint>, initialBalance: Double): Double {
        if (equity.size < 3) return 0.0
        val returns = ArrayList<Double>(equity.size - 1)

        var prev = max(1e-9, equity.first().equity)
        for (i in 1 until equity.size) {
            val cur = max(1e-9, equity[i].equity)
            val r = (cur - prev) / prev
            returns.add(r)
            prev = cur
        }

        val mean = returns.average()
        var variance = 0.0
        for (r in returns) {
            val d = r - mean
            variance += d * d
        }
        variance /= max(1, returns.size - 1)
        val std = sqrt(variance)
        if (std <= 1e-12) return 0.0

        // not annualized; "like" ratio for comparing runs.
        return mean / std
    }
}
