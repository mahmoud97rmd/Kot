package com.tradingapp.metatrader.core.engine.backtest

import com.tradingapp.metatrader.domain.models.Candle
import com.tradingapp.metatrader.domain.models.trading.Position
import kotlin.math.abs
import kotlin.math.max

class SimpleBacktestEngine(
    private val initialBalance: Double = 10_000.0
) {
    data class Trade(
        val side: Position.Side,
        val entryPrice: Double,
        val exitPrice: Double,
        val profit: Double
    )

    data class Report(
        val totalTrades: Int,
        val netProfit: Double,
        val winRate: Double,
        val maxDrawdown: Double,
        val equityCurve: List<Double>
    )

    fun run(
        candles: List<Candle>,
        decide: (index: Int, candle: Candle, state: State) -> Decision
    ): Pair<Report, List<Trade>> {
        if (candles.isEmpty()) {
            return Report(0, 0.0, 0.0, 0.0, emptyList()) to emptyList()
        }

        val trades = mutableListOf<Trade>()
        val equity = ArrayList<Double>(candles.size)

        var balance = initialBalance
        var peak = initialBalance
        var maxDD = 0.0

        var open: OpenPosition? = null

        for (i in candles.indices) {
            val c = candles[i]

            // TP/SL check (worst-case)
            open?.let { op ->
                val hit = op.checkHit(c)
                if (hit != null) {
                    val profit = op.profitAt(hit)
                    balance += profit
                    trades += Trade(op.side, op.entry, hit, profit)
                    open = null
                }
            }

            val decision = decide(i, c, State(balance, open != null))
            when (decision) {
                is Decision.Open -> {
                    if (open == null) {
                        open = OpenPosition(
                            side = decision.side,
                            entry = c.close,
                            sl = decision.sl,
                            tp = decision.tp,
                            lots = max(0.0001, decision.lots)
                        )
                    }
                }
                Decision.Close -> {
                    open?.let { op ->
                        val profit = op.profitAt(c.close)
                        balance += profit
                        trades += Trade(op.side, op.entry, c.close, profit)
                        open = null
                    }
                }
                Decision.None -> Unit
            }

            peak = max(peak, balance)
            val dd = peak - balance
            maxDD = max(maxDD, dd)

            equity.add(balance)
        }

        val net = balance - initialBalance
        val wins = trades.count { it.profit > 0 }
        val winRate = if (trades.isEmpty()) 0.0 else wins.toDouble() / trades.size.toDouble()

        val report = Report(
            totalTrades = trades.size,
            netProfit = net,
            winRate = winRate,
            maxDrawdown = maxDD,
            equityCurve = equity
        )
        return report to trades
    }

    data class State(val balance: Double, val hasOpen: Boolean)

    sealed class Decision {
        data class Open(val side: Position.Side, val lots: Double, val sl: Double?, val tp: Double?) : Decision()
        object Close : Decision()
        object None : Decision()
    }

    private data class OpenPosition(
        val side: Position.Side,
        val entry: Double,
        val sl: Double?,
        val tp: Double?,
        val lots: Double
    ) {
        fun checkHit(c: Candle): Double? {
            return when (side) {
                Position.Side.BUY -> {
                    if (sl != null && c.low <= sl) sl
                    else if (tp != null && c.high >= tp) tp
                    else null
                }
                Position.Side.SELL -> {
                    if (sl != null && c.high >= sl) sl
                    else if (tp != null && c.low <= tp) tp
                    else null
                }
            }
        }

        fun profitAt(exit: Double): Double {
            val points = when (side) {
                Position.Side.BUY -> (exit - entry)
                Position.Side.SELL -> (entry - exit)
            }
            return points * lots * 100.0
        }
    }
}
