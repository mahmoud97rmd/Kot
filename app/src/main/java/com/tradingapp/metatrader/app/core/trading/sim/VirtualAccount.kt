package com.tradingapp.metatrader.app.core.trading.sim

import java.util.UUID

class VirtualAccount(
    var balance: Double = 10_000.0,
    private val contractMultiplier: Double = 100.0
) {
    private val _openPositions = ArrayList<Position>()
    private val _history = ArrayList<ClosedTrade>()

    val openPositions: List<Position> get() = _openPositions
    val history: List<ClosedTrade> get() = _history

    fun equity(currentPrice: Double): Double {
        return balance + floatingPnL(currentPrice)
    }

    fun floatingPnL(currentPrice: Double): Double {
        var sum = 0.0
        for (p in _openPositions) {
            sum += profitOf(p.side, p.entryPrice, currentPrice, p.lots)
        }
        return sum
    }

    fun open(side: Side, price: Double, lots: Double, timeSec: Long): Position {
        val pos = Position(
            id = UUID.randomUUID().toString(),
            side = side,
            entryPrice = price,
            lots = lots,
            openedAtSec = timeSec
        )
        _openPositions.add(pos)
        return pos
    }

    fun closeAll(price: Double, timeSec: Long): List<ClosedTrade> {
        val closed = ArrayList<ClosedTrade>()
        val it = _openPositions.iterator()
        while (it.hasNext()) {
            val p = it.next()
            it.remove()
            val profit = profitOf(p.side, p.entryPrice, price, p.lots)
            balance += profit
            val tr = ClosedTrade(
                id = p.id,
                side = p.side,
                entryPrice = p.entryPrice,
                exitPrice = price,
                lots = p.lots,
                openedAtSec = p.openedAtSec,
                closedAtSec = timeSec,
                profit = profit
            )
            _history.add(tr)
            closed.add(tr)
        }
        return closed
    }

    private fun profitOf(side: Side, entry: Double, exit: Double, lots: Double): Double {
        val diff = if (side == Side.BUY) (exit - entry) else (entry - exit)
        return diff * lots * contractMultiplier
    }
}
