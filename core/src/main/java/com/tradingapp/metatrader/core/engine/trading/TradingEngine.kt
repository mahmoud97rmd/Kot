package com.tradingapp.metatrader.core.engine.trading

import com.tradingapp.metatrader.domain.models.trading.AccountSnapshot
import com.tradingapp.metatrader.domain.models.trading.ClosedTrade
import com.tradingapp.metatrader.domain.models.trading.PendingOrder
import com.tradingapp.metatrader.domain.models.trading.Position
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.util.UUID

class TradingEngine(
    initialBalance: Double = 10_000.0
) {
    sealed class Event {
        data class PositionOpened(val position: Position) : Event()
        data class PositionClosed(val trade: ClosedTrade) : Event()
        data class PositionModified(val position: Position) : Event()

        data class PendingPlaced(val order: PendingOrder) : Event()
        data class PendingCanceled(val orderId: String) : Event()
        data class PendingModified(val order: PendingOrder) : Event()
        data class PendingTriggered(val orderId: String, val openedPositionId: String) : Event()

        data class AccountUpdated(val snapshot: AccountSnapshot) : Event()
    }

    private val exchange = VirtualExchange(initialBalance)

    private val _account = MutableStateFlow(AccountSnapshot(exchange.balance, exchange.equity))
    val account: StateFlow<AccountSnapshot> = _account.asStateFlow()

    private val _positions = MutableStateFlow<List<Position>>(emptyList())
    val positions: StateFlow<List<Position>> = _positions.asStateFlow()

    private val _history = MutableStateFlow<List<ClosedTrade>>(emptyList())
    val history: StateFlow<List<ClosedTrade>> = _history.asStateFlow()

    private val _pending = MutableStateFlow<List<PendingOrder>>(emptyList())
    val pending: StateFlow<List<PendingOrder>> = _pending.asStateFlow()

    private val _events = MutableSharedFlow<Event>(extraBufferCapacity = 256)
    val events: SharedFlow<Event> = _events.asSharedFlow()

    fun onTick(time: Instant, bid: Double, ask: Double) {
        // 1) Pending triggers
        val triggered = mutableListOf<PendingOrder>()
        val remain = mutableListOf<PendingOrder>()

        for (o in _pending.value) {
            val shouldTrigger = when (o.type) {
                PendingOrder.Type.BUY_LIMIT -> ask <= o.targetPrice
                PendingOrder.Type.SELL_LIMIT -> bid >= o.targetPrice
                PendingOrder.Type.BUY_STOP -> ask >= o.targetPrice
                PendingOrder.Type.SELL_STOP -> bid <= o.targetPrice
            }
            if (shouldTrigger) triggered.add(o) else remain.add(o)
        }

        if (triggered.isNotEmpty()) {
            _pending.value = remain
            for (o in triggered) {
                val side = when (o.type) {
                    PendingOrder.Type.BUY_LIMIT, PendingOrder.Type.BUY_STOP -> Position.Side.BUY
                    PendingOrder.Type.SELL_LIMIT, PendingOrder.Type.SELL_STOP -> Position.Side.SELL
                }
                val execPrice = if (side == Position.Side.BUY) ask else bid
                val pos = placeMarketOrderInternal(
                    instrument = o.instrument,
                    side = side,
                    time = time,
                    price = execPrice,
                    lots = o.lots,
                    sl = o.stopLoss,
                    tp = o.takeProfit,
                    comment = o.comment
                )
                _events.tryEmit(Event.PendingTriggered(o.id, pos.id))
            }
        }

        // 2) SL/TP and equity
        val beforeIds = exchange.getOpenPositions().map { it.id }.toSet()
        exchange.onPrice(time, bid, ask)
        val afterIds = exchange.getOpenPositions().map { it.id }.toSet()

        if (beforeIds != afterIds) {
            val latest = exchange.getHistory().lastOrNull()
            if (latest != null) _events.tryEmit(Event.PositionClosed(latest.toDomain()))
        }

        _positions.value = exchange.getOpenPositions().map { it.toDomain() }
        _history.value = exchange.getHistory().map { it.toDomain() }

        val snap = AccountSnapshot(exchange.balance, exchange.equity)
        _account.value = snap
        _events.tryEmit(Event.AccountUpdated(snap))
    }

    fun placeMarketOrder(
        instrument: String,
        side: Position.Side,
        time: Instant,
        price: Double,
        lots: Double,
        sl: Double?,
        tp: Double?,
        comment: String?
    ): Position = placeMarketOrderInternal(instrument, side, time, price, lots, sl, tp, comment)

    private fun placeMarketOrderInternal(
        instrument: String,
        side: Position.Side,
        time: Instant,
        price: Double,
        lots: Double,
        sl: Double?,
        tp: Double?,
        comment: String?
    ): Position {
        val pos = exchange.placeMarketOrder(
            instrument = instrument,
            side = if (side == Position.Side.BUY) VirtualExchange.Side.BUY else VirtualExchange.Side.SELL,
            time = time,
            price = price,
            lots = lots,
            sl = sl,
            tp = tp,
            comment = comment
        ).toDomain()

        _positions.value = exchange.getOpenPositions().map { it.toDomain() }
        _events.tryEmit(Event.PositionOpened(pos))
        return pos
    }

    fun closePosition(positionId: String, time: Instant, price: Double): ClosedTrade? {
        val closed = exchange.closePosition(positionId, time, price)?.toDomain() ?: return null
        _events.tryEmit(Event.PositionClosed(closed))
        _positions.value = exchange.getOpenPositions().map { it.toDomain() }
        _history.value = exchange.getHistory().map { it.toDomain() }
        return closed
    }

    fun modifyPosition(positionId: String, newSl: Double?, newTp: Double?): Position? {
        val updated = exchange.modifyPositionRisk(positionId, newSl, newTp)?.toDomain() ?: return null
        _positions.value = exchange.getOpenPositions().map { it.toDomain() }
        _events.tryEmit(Event.PositionModified(updated))
        return updated
    }

    fun placePendingOrder(
        instrument: String,
        type: PendingOrder.Type,
        time: Instant,
        targetPrice: Double,
        lots: Double,
        sl: Double?,
        tp: Double?,
        comment: String?
    ): PendingOrder {
        val order = PendingOrder(
            id = UUID.randomUUID().toString(),
            instrument = instrument,
            type = type,
            createdAt = time,
            targetPrice = targetPrice,
            lots = lots,
            stopLoss = sl,
            takeProfit = tp,
            comment = comment
        )
        _pending.value = _pending.value + order
        _events.tryEmit(Event.PendingPlaced(order))
        return order
    }

    fun cancelPending(orderId: String) {
        val before = _pending.value
        val after = before.filterNot { it.id == orderId }
        if (after.size != before.size) {
            _pending.value = after
            _events.tryEmit(Event.PendingCanceled(orderId))
        }
    }

    fun modifyPending(orderId: String, newTarget: Double, newSl: Double?, newTp: Double?): PendingOrder? {
        val list = _pending.value.toMutableList()
        val idx = list.indexOfFirst { it.id == orderId }
        if (idx == -1) return null
        val updated = list[idx].copy(targetPrice = newTarget, stopLoss = newSl, takeProfit = newTp)
        list[idx] = updated
        _pending.value = list.toList()
        _events.tryEmit(Event.PendingModified(updated))
        return updated
    }

    private fun VirtualExchange.Position.toDomain(): Position =
        Position(
            id = id,
            instrument = instrument,
            side = if (side == VirtualExchange.Side.BUY) Position.Side.BUY else Position.Side.SELL,
            entryTime = entryTime,
            entryPrice = entryPrice,
            lots = lots,
            stopLoss = stopLoss,
            takeProfit = takeProfit,
            comment = comment
        )

    private fun VirtualExchange.ClosedTrade.toDomain(): ClosedTrade =
        ClosedTrade(
            id = id,
            instrument = instrument,
            side = if (side == VirtualExchange.Side.BUY) Position.Side.BUY else Position.Side.SELL,
            entryTime = entryTime,
            exitTime = exitTime,
            entryPrice = entryPrice,
            exitPrice = exitPrice,
            lots = lots,
            profit = profit,
            comment = comment
        )
}
