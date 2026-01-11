package com.tradingapp.metatrader.app.features.expert.engine.broker

import com.tradingapp.metatrader.domain.models.backtest.BacktestSide
import java.util.UUID
import kotlin.math.abs

class SimpleSimBroker(
    private val initialBalance: Double,
    private val pointValue: Double,
    private val spreadPoints: Double,
    private val commissionPerLot: Double
) : BrokerPort {

    private var _balance: Double = initialBalance
    private val openPositions = LinkedHashMap<String, BrokerPort.Position>()
    private val closed = ArrayList<BrokerPort.CloseResult>()

    override fun positions(): List<BrokerPort.Position> = openPositions.values.toList()

    override fun openMarket(
        side: BacktestSide,
        lots: Double,
        price: Double,
        timeSec: Long,
        sl: Double?,
        tp: Double?,
        comment: String?
    ): String {
        require(lots > 0.0) { "lots must be > 0" }

        val id = UUID.randomUUID().toString()

        // subtract commission upfront
        _balance -= abs(lots) * commissionPerLot

        openPositions[id] = BrokerPort.Position(
            id = id,
            side = side,
            lots = lots,
            entryTimeSec = timeSec,
            entryPrice = price,
            stopLoss = sl,
            takeProfit = tp,
            comment = comment
        )
        return id
    }

    override fun close(positionId: String, price: Double, timeSec: Long): BrokerPort.CloseResult? {
        val pos = openPositions.remove(positionId) ?: return null
        val profit = calcProfit(pos, exitPrice = price)

        _balance += profit

        val r = BrokerPort.CloseResult(
            positionId = positionId,
            exitTimeSec = timeSec,
            exitPrice = price,
            profit = profit
        )
        closed.add(r)
        return r
    }

    override fun updateOnPrice(timeSec: Long, bid: Double, ask: Double): List<BrokerPort.CloseResult> {
        if (openPositions.isEmpty()) return emptyList()

        val toClose = ArrayList<String>()
        for ((id, p) in openPositions) {
            val price = if (p.side == BacktestSide.BUY) bid else ask

            val hitSl = p.stopLoss?.let { sl ->
                if (p.side == BacktestSide.BUY) price <= sl else price >= sl
            } ?: false

            val hitTp = p.takeProfit?.let { tp ->
                if (p.side == BacktestSide.BUY) price >= tp else price <= tp
            } ?: false

            if (hitSl || hitTp) toClose.add(id)
        }

        if (toClose.isEmpty()) return emptyList()

        val results = ArrayList<BrokerPort.CloseResult>(toClose.size)
        for (id in toClose) {
            val p = openPositions[id] ?: continue
            val exitPrice = if (p.side == BacktestSide.BUY) bid else ask
            close(id, exitPrice, timeSec)?.let { results.add(it) }
        }
        return results
    }

    override fun equity(): Double {
        // simple: equity == balance (no floating P/L for now)
        return _balance
    }

    override fun balance(): Double = _balance

    private fun calcProfit(pos: BrokerPort.Position, exitPrice: Double): Double {
        // profit = (delta price in points) * lots * pointValue
        val delta = if (pos.side == BacktestSide.BUY) (exitPrice - pos.entryPrice) else (pos.entryPrice - exitPrice)
        val points = delta // here already in price units; pointValue converts for your instrument
        return points * pos.lots * pointValue
    }
}
