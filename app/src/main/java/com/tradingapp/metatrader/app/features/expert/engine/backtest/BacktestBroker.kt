package com.tradingapp.metatrader.app.features.expert.engine.backtest

import com.tradingapp.metatrader.app.core.trading.OrderSide
import com.tradingapp.metatrader.domain.models.backtest.BacktestTrade
import java.util.UUID
import kotlin.math.abs

class BacktestBroker(
    private val pointValue: Double,
    private val commissionPerLot: Double,
    private val spreadPoints: Double
) {

    data class Position(
        val id: String,
        val side: OrderSide,
        val units: Long,
        val entryTimeSec: Long,
        val entryPrice: Double,
        val tp: Double?,
        val sl: Double?
    )

    private var open: Position? = null
    private val trades = ArrayList<BacktestTrade>()

    fun openPositionsCount(): Int = if (open != null) 1 else 0

    fun placeMarket(side: OrderSide, units: Long, timeSec: Long, price: Double, tp: Double?, sl: Double?) : Boolean {
        if (open != null) return false

        val spreadAdj = (spreadPoints * pointValue)
        val fill = when (side) {
            OrderSide.BUY -> price + spreadAdj
            OrderSide.SELL -> price - spreadAdj
        }

        open = Position(
            id = UUID.randomUUID().toString(),
            side = side,
            units = abs(units),
            entryTimeSec = timeSec,
            entryPrice = fill,
            tp = tp,
            sl = sl
        )
        return true
    }

    fun closeAll(timeSec: Long, price: Double) {
        val p = open ?: return
        val exit = price
        closePosition(p, timeSec, exit, reason = "CLOSE_ALL")
        open = null
    }

    fun onBar(timeSec: Long, openPrice: Double, high: Double, low: Double, close: Double) {
        val p = open ?: return

        // Determine if TP/SL hit within bar using high/low.
        val tp = p.tp
        val sl = p.sl

        var exitPrice: Double? = null
        var reason = ""

        if (p.side == OrderSide.BUY) {
            if (tp != null && high >= tp) { exitPrice = tp; reason = "TP" }
            else if (sl != null && low <= sl) { exitPrice = sl; reason = "SL" }
        } else {
            if (tp != null && low <= tp) { exitPrice = tp; reason = "TP" }
            else if (sl != null && high >= sl) { exitPrice = sl; reason = "SL" }
        }

        if (exitPrice != null) {
            closePosition(p, timeSec, exitPrice, reason)
            open = null
        }
    }

    fun getTrades(): List<BacktestTrade> = trades.toList()

    private fun closePosition(p: Position, exitTimeSec: Long, exitPrice: Double, reason: String) {
        val direction = if (p.side == OrderSide.BUY) 1.0 else -1.0
        val pnl = (exitPrice - p.entryPrice) * direction * p.units.toDouble()

        // Very simple commission: commissionPerLot * (units/100000) if forex style.
        val lots = p.units.toDouble() / 100000.0
        val commission = commissionPerLot * lots

        val net = pnl - commission

        trades.add(
            BacktestTrade(
                id = p.id,
                side = if (p.side == OrderSide.BUY) "BUY" else "SELL",
                entryTimeSec = p.entryTimeSec,
                exitTimeSec = exitTimeSec,
                entryPrice = p.entryPrice,
                exitPrice = exitPrice,
                profit = net,
                reason = reason
            )
        )
    }
}
