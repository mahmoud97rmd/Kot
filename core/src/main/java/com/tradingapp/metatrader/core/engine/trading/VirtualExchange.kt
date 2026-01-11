package com.tradingapp.metatrader.core.engine.trading

import java.time.Instant
import java.util.UUID

class VirtualExchange(
    initialBalance: Double = 10_000.0
) {
    enum class Side { BUY, SELL }

    data class Position(
        val id: String = UUID.randomUUID().toString(),
        val instrument: String,
        val side: Side,
        val entryTime: Instant,
        val entryPrice: Double,
        val lots: Double,
        val stopLoss: Double?,
        val takeProfit: Double?,
        val comment: String? = null
    )

    data class ClosedTrade(
        val id: String,
        val instrument: String,
        val side: Side,
        val entryTime: Instant,
        val exitTime: Instant,
        val entryPrice: Double,
        val exitPrice: Double,
        val lots: Double,
        val profit: Double,
        val comment: String?
    )

    var balance: Double = initialBalance
        private set

    var equity: Double = initialBalance
        private set

    private val openPositions = mutableListOf<Position>()
    private val history = mutableListOf<ClosedTrade>()

    fun getOpenPositions(): List<Position> = openPositions.toList()
    fun getHistory(): List<ClosedTrade> = history.toList()

    fun placeMarketOrder(
        instrument: String,
        side: Side,
        time: Instant,
        price: Double,
        lots: Double,
        sl: Double? = null,
        tp: Double? = null,
        comment: String? = null
    ): Position {
        val pos = Position(
            instrument = instrument,
            side = side,
            entryTime = time,
            entryPrice = price,
            lots = lots,
            stopLoss = sl,
            takeProfit = tp,
            comment = comment
        )
        openPositions.add(pos)
        return pos
    }

    fun modifyPositionRisk(positionId: String, newSl: Double?, newTp: Double?): Position? {
        val idx = openPositions.indexOfFirst { it.id == positionId }
        if (idx == -1) return null
        val old = openPositions[idx]
        val updated = old.copy(stopLoss = newSl, takeProfit = newTp)
        openPositions[idx] = updated
        return updated
    }

    fun onPrice(time: Instant, bid: Double, ask: Double) {
        var floating = 0.0
        val toClose = mutableListOf<Pair<Position, Double>>()

        for (p in openPositions) {
            val exitPriceMark = if (p.side == Side.BUY) bid else ask
            val pl = profitOf(p, exitPriceMark)
            floating += pl

            if (p.side == Side.BUY) {
                if (p.takeProfit != null && exitPriceMark >= p.takeProfit) toClose.add(p to p.takeProfit)
                if (p.stopLoss != null && exitPriceMark <= p.stopLoss) toClose.add(p to p.stopLoss)
            } else {
                if (p.takeProfit != null && exitPriceMark <= p.takeProfit) toClose.add(p to p.takeProfit)
                if (p.stopLoss != null && exitPriceMark >= p.stopLoss) toClose.add(p to p.stopLoss)
            }
        }

        equity = balance + floating

        for ((p, exitPrice) in toClose.distinctBy { it.first.id }) {
            closePosition(p.id, time, exitPrice)
        }
    }

    fun closePosition(positionId: String, time: Instant, exitPrice: Double): ClosedTrade? {
        val idx = openPositions.indexOfFirst { it.id == positionId }
        if (idx == -1) return null
        val p = openPositions.removeAt(idx)
        val profit = profitOf(p, exitPrice)
        balance += profit
        val closed = ClosedTrade(
            id = p.id,
            instrument = p.instrument,
            side = p.side,
            entryTime = p.entryTime,
            exitTime = time,
            entryPrice = p.entryPrice,
            exitPrice = exitPrice,
            lots = p.lots,
            profit = profit,
            comment = p.comment
        )
        history.add(closed)
        equity = balance
        return closed
    }

    private fun profitOf(p: Position, exitPrice: Double): Double {
        val points = if (p.side == Side.BUY) (exitPrice - p.entryPrice) else (p.entryPrice - exitPrice)
        return points * p.lots * 100.0
    }
}
