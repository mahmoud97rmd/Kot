package com.tradingapp.metatrader.app.core.trading.mt5sim

import java.util.UUID
import kotlin.math.abs

/**
 * MT5-like simulated account:
 * - Market orders
 * - Pending orders (Limit/Stop)
 * - SL/TP/Trailing
 * - Deals history
 *
 * NOTE: integrate this into your existing VirtualAccountMt5 by copying the added parts:
 * pendingOrders + place/cancel/checkPendingOnQuote
 */
class VirtualAccountMt5(
    balance: Double
) {
    var balance: Double = balance
        private set

    val positions: MutableList<PositionMt5> = mutableListOf()
    val history: MutableList<DealMt5> = mutableListOf()

    // NEW
    val pendingOrders: MutableList<PendingOrderMt5> = mutableListOf()

    fun equity(currentQuote: PriceQuote? = null, spec: InstrumentSpec? = null, symbol: String? = null): Double {
        var eq = balance
        if (currentQuote != null && spec != null && symbol != null) {
            for (p in positions) {
                if (p.symbol != symbol) continue
                eq += floatingPnl(spec, p, currentQuote)
            }
        }
        return eq
    }

    fun openMarket(
        spec: InstrumentSpec,
        symbol: String,
        side: Side,
        lots: Double,
        quote: PriceQuote,
        sl: Double? = null,
        tp: Double? = null,
        trailingStopPips: Double? = null,
        comment: String? = null
    ): PositionMt5 {
        require(lots > 0.0) { "lots must be > 0" }
        val entry = if (side == Side.BUY) quote.ask else quote.bid
        val pos = PositionMt5(
            id = UUID.randomUUID().toString(),
            symbol = symbol,
            side = side,
            lots = lots,
            entryPrice = entry,
            openTimeSec = quote.timeSec,
            stopLoss = sl,
            takeProfit = tp,
            trailingStopPips = trailingStopPips,
            comment = comment
        )
        positions.add(pos)
        return pos
    }

    fun closePartial(
        spec: InstrumentSpec,
        positionId: String,
        closeLots: Double,
        quote: PriceQuote,
        reason: String = "MANUAL"
    ): DealMt5? {
        val idx = positions.indexOfFirst { it.id == positionId }
        if (idx < 0) return null
        val pos = positions[idx]
        require(closeLots > 0.0 && closeLots <= pos.lots) { "invalid closeLots" }

        val exitPrice = if (pos.side == Side.BUY) quote.bid else quote.ask
        val profit = realizedPnl(spec, pos, closeLots, exitPrice)

        val deal = DealMt5(
            id = UUID.randomUUID().toString(),
            symbol = pos.symbol,
            side = pos.side,
            lots = closeLots,
            entryPrice = pos.entryPrice,
            exitPrice = exitPrice,
            openTimeSec = pos.openTimeSec,
            closeTimeSec = quote.timeSec,
            profit = profit,
            commission = 0.0,
            reason = reason
        )
        history.add(deal)
        balance += profit

        val remaining = pos.lots - closeLots
        if (remaining <= 1e-12) positions.removeAt(idx)
        else positions[idx] = pos.copy(lots = remaining)

        return deal
    }

    // ---------------- NEW: Pending Orders ----------------

    fun placePending(
        symbol: String,
        type: PendingType,
        lots: Double,
        entryPrice: Double,
        sl: Double? = null,
        tp: Double? = null,
        comment: String? = null,
        createdTimeSec: Long
    ): PendingOrderMt5 {
        require(lots > 0.0) { "lots must be > 0" }
        val po = PendingOrderMt5(
            symbol = symbol,
            type = type,
            lots = lots,
            entryPrice = entryPrice,
            stopLoss = sl,
            takeProfit = tp,
            comment = comment,
            createdTimeSec = createdTimeSec
        )
        pendingOrders.add(po)
        return po
    }

    fun cancelPending(orderId: String): Boolean {
        val idx = pendingOrders.indexOfFirst { it.id == orderId }
        if (idx < 0) return false
        pendingOrders.removeAt(idx)
        return true
    }

    /**
     * Execute pending orders when quote crosses trigger.
     * - BUY_LIMIT: execute when ask <= entryPrice
     * - SELL_LIMIT: execute when bid >= entryPrice
     * - BUY_STOP: execute when ask >= entryPrice
     * - SELL_STOP: execute when bid <= entryPrice
     */
    fun checkPendingOnQuote(spec: InstrumentSpec, quote: PriceQuote): List<PositionMt5> {
        if (pendingOrders.isEmpty()) return emptyList()

        val executed = mutableListOf<PositionMt5>()
        val it = pendingOrders.iterator()
        while (it.hasNext()) {
            val po = it.next()
            val bid = quote.bid
            val ask = quote.ask

            val shouldExecute = when (po.type) {
                PendingType.BUY_LIMIT -> ask <= po.entryPrice
                PendingType.SELL_LIMIT -> bid >= po.entryPrice
                PendingType.BUY_STOP -> ask >= po.entryPrice
                PendingType.SELL_STOP -> bid <= po.entryPrice
            }

            if (!shouldExecute) continue

            val side = when (po.type) {
                PendingType.BUY_LIMIT, PendingType.BUY_STOP -> Side.BUY
                PendingType.SELL_LIMIT, PendingType.SELL_STOP -> Side.SELL
            }

            // execute as market at current quote (realistic slippage simulation can be added later)
            val pos = openMarket(
                spec = spec,
                symbol = po.symbol,
                side = side,
                lots = po.lots,
                quote = quote,
                sl = po.stopLoss,
                tp = po.takeProfit,
                trailingStopPips = null,
                comment = "PENDING:${po.type}" + (po.comment?.let { " $it" } ?: "")
            )
            executed.add(pos)
            it.remove()
        }
        return executed
    }

    // ---------------- Stops/Trailing (existing logic hooks) ----------------

    fun checkStopsOnCandle(
        spec: InstrumentSpec,
        candleTimeSec: Long,
        candleHigh: Double,
        candleLow: Double,
        candleClose: Double,
        conservativeWorstCase: Boolean
    ): List<DealMt5> {
        val deals = mutableListOf<DealMt5>()
        val quote = PriceQuote(timeSec = candleTimeSec, bid = candleClose, ask = candleClose)

        val it = positions.iterator()
        while (it.hasNext()) {
            val p = it.next()

            // trailing update
            val tr = p.trailingStopPips
            val updated = if (tr != null) {
                applyTrailing(spec, p, candleHigh, candleLow, tr)
            } else p

            // stop/limit checks
            val stopHit = updated.stopLoss?.let { sl ->
                when (updated.side) {
                    Side.BUY -> candleLow <= sl
                    Side.SELL -> candleHigh >= sl
                }
            } ?: false

            val tpHit = updated.takeProfit?.let { tp ->
                when (updated.side) {
                    Side.BUY -> candleHigh >= tp
                    Side.SELL -> candleLow <= tp
                }
            } ?: false

            if (!stopHit && !tpHit) {
                // keep updated trailing
                if (updated != p) {
                    // replace in list
                    val idx = positions.indexOfFirst { it.id == p.id }
                    if (idx >= 0) positions[idx] = updated
                }
                continue
            }

            val reason = when {
                stopHit && tpHit -> if (conservativeWorstCase) "SL" else "TP"
                stopHit -> "SL"
                else -> "TP"
            }

            val closePrice = when (reason) {
                "SL" -> updated.stopLoss!!
                "TP" -> updated.takeProfit!!
                else -> candleClose
            }

            val profit = realizedPnl(spec, updated, updated.lots, closePrice)

            val deal = DealMt5(
                id = UUID.randomUUID().toString(),
                symbol = updated.symbol,
                side = updated.side,
                lots = updated.lots,
                entryPrice = updated.entryPrice,
                exitPrice = closePrice,
                openTimeSec = updated.openTimeSec,
                closeTimeSec = candleTimeSec,
                profit = profit,
                commission = 0.0,
                reason = reason
            )
            history.add(deal)
            balance += profit
            deals.add(deal)
            it.remove()
        }

        return deals
    }

    private fun applyTrailing(spec: InstrumentSpec, p: PositionMt5, candleHigh: Double, candleLow: Double, trailingPips: Double): PositionMt5 {
        val trail = trailingPips * spec.pip
        return when (p.side) {
            Side.BUY -> {
                val newSl = candleHigh - trail
                val curSl = p.stopLoss
                if (curSl == null || newSl > curSl) p.copy(stopLoss = newSl) else p
            }
            Side.SELL -> {
                val newSl = candleLow + trail
                val curSl = p.stopLoss
                if (curSl == null || newSl < curSl) p.copy(stopLoss = newSl) else p
            }
        }
    }

    private fun realizedPnl(spec: InstrumentSpec, p: PositionMt5, lots: Double, exitPrice: Double): Double {
        val diff = when (p.side) {
            Side.BUY -> (exitPrice - p.entryPrice)
            Side.SELL -> (p.entryPrice - exitPrice)
        }
        return diff * spec.contractSize * lots
    }

    private fun floatingPnl(spec: InstrumentSpec, p: PositionMt5, quote: PriceQuote): Double {
        val mkt = when (p.side) {
            Side.BUY -> quote.bid
            Side.SELL -> quote.ask
        }
        return realizedPnl(spec, p, p.lots, mkt)
    }
}
