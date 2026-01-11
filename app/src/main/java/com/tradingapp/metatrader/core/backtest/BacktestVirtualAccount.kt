package com.tradingapp.metatrader.core.backtest

import com.tradingapp.metatrader.domain.models.backtest.BacktestConfig
import com.tradingapp.metatrader.domain.models.backtest.BacktestOrder
import com.tradingapp.metatrader.domain.models.backtest.BacktestSide
import com.tradingapp.metatrader.domain.models.backtest.BacktestTrade
import java.util.UUID
import kotlin.math.abs

class BacktestVirtualAccount(
    private val config: BacktestConfig
) {
    var balance: Double = config.initialBalance
        private set

    private data class OpenPosition(
        val id: String,
        val side: BacktestSide,
        val lots: Double,
        var entryPrice: Double,
        val entryTimeSec: Long,
        var stopLoss: Double?,
        var takeProfit: Double?
    )

    private val open = mutableListOf<OpenPosition>()
    private val history = mutableListOf<BacktestTrade>()

    fun openPositionsCount(): Int = open.size
    fun tradeHistory(): List<BacktestTrade> = history.toList()

    fun execute(order: BacktestOrder) {
        // Commission on entry
        val commission = config.commissionPerLot * order.lots
        balance -= commission

        open.add(
            OpenPosition(
                id = UUID.randomUUID().toString(),
                side = order.side,
                lots = order.lots,
                entryPrice = order.entryPrice,
                entryTimeSec = order.timeSec,
                stopLoss = order.stopLoss,
                takeProfit = order.takeProfit
            )
        )
    }

    fun onCandle(high: Double, low: Double, close: Double, timeSec: Long) {
        // Check SL/TP with intra-candle extremes (more realistic than close-only)
        val toClose = mutableListOf<Pair<OpenPosition, Double>>() // pos to exitPrice

        for (p in open) {
            val sl = p.stopLoss
            val tp = p.takeProfit

            when (p.side) {
                BacktestSide.BUY -> {
                    // SL hit if low <= sl
                    if (sl != null && low <= sl) {
                        toClose.add(p to sl)
                        continue
                    }
                    // TP hit if high >= tp
                    if (tp != null && high >= tp) {
                        toClose.add(p to tp)
                        continue
                    }
                }
                BacktestSide.SELL -> {
                    // SL hit if high >= sl (for sell, SL is above entry typically)
                    if (sl != null && high >= sl) {
                        toClose.add(p to sl)
                        continue
                    }
                    // TP hit if low <= tp
                    if (tp != null && low <= tp) {
                        toClose.add(p to tp)
                        continue
                    }
                }
            }
        }

        // Close triggered positions
        for ((p, exitPriceRaw) in toClose) {
            closePosition(p, exitPriceRaw, timeSec)
        }

        // Optionally: update floating P/L via close (not stored but could be)
        // We keep balance only realized.
        @Suppress("UNUSED_VARIABLE")
        val floating = open.sumOf { pos ->
            val pnl = when (pos.side) {
                BacktestSide.BUY -> (close - pos.entryPrice) * pos.lots
                BacktestSide.SELL -> (pos.entryPrice - close) * pos.lots
            }
            pnl
        }
    }

    fun closeAll(closePrice: Double, timeSec: Long) {
        val copy = open.toList()
        for (p in copy) closePosition(p, closePrice, timeSec)
    }

    private fun closePosition(p: OpenPosition, exitPriceRaw: Double, timeSec: Long) {
        open.remove(p)

        val profit = when (p.side) {
            BacktestSide.BUY -> (exitPriceRaw - p.entryPrice) * p.lots
            BacktestSide.SELL -> (p.entryPrice - exitPriceRaw) * p.lots
        }

        // Commission on exit
        val commission = config.commissionPerLot * p.lots
        val net = profit - commission
        balance += net

        history.add(
            BacktestTrade(
                id = p.id,
                side = p.side,
                lots = p.lots,
                entryPrice = p.entryPrice,
                entryTimeSec = p.entryTimeSec,
                exitPrice = exitPriceRaw,
                exitTimeSec = timeSec,
                profit = net,
                stopLoss = p.stopLoss,
                takeProfit = p.takeProfit
            )
        )
    }
}
