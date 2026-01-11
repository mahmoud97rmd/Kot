package com.tradingapp.metatrader.data.repository

import com.tradingapp.metatrader.core.engine.trading.TradingEngine
import com.tradingapp.metatrader.data.local.database.dao.ClosedTradeDao
import com.tradingapp.metatrader.data.local.database.dao.PendingOrderDao
import com.tradingapp.metatrader.data.local.database.dao.PositionDao
import com.tradingapp.metatrader.data.mappers.toDomain
import com.tradingapp.metatrader.data.mappers.toEntity
import com.tradingapp.metatrader.domain.models.trading.AccountSnapshot
import com.tradingapp.metatrader.domain.models.trading.PendingOrder
import com.tradingapp.metatrader.domain.models.trading.Position
import com.tradingapp.metatrader.domain.models.trading.TradingEvent
import com.tradingapp.metatrader.domain.repository.TradingEngineInput
import com.tradingapp.metatrader.domain.repository.TradingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Instant

class TradingRepositoryImpl(
    private val engine: TradingEngine,
    private val positionDao: PositionDao,
    private val closedDao: ClosedTradeDao,
    private val pendingDao: PendingOrderDao
) : TradingRepository, TradingEngineInput {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _events = MutableSharedFlow<TradingEvent>(extraBufferCapacity = 256)
    private val eventsFlow = _events.asSharedFlow()

    init {
        scope.launch {
            engine.events.collect { ev ->
                when (ev) {
                    is TradingEngine.Event.PositionOpened -> {
                        positionDao.upsert(ev.position.toEntity())
                        _events.tryEmit(TradingEvent.PositionOpened(ev.position))
                    }
                    is TradingEngine.Event.PositionModified -> {
                        positionDao.upsert(ev.position.toEntity())
                        _events.tryEmit(TradingEvent.PositionModified(ev.position))
                    }
                    is TradingEngine.Event.PositionClosed -> {
                        positionDao.delete(ev.trade.id)
                        closedDao.upsert(ev.trade.toEntity())
                        _events.tryEmit(TradingEvent.PositionClosed(ev.trade))
                    }

                    is TradingEngine.Event.PendingPlaced -> {
                        pendingDao.upsert(ev.order.toEntity())
                        _events.tryEmit(TradingEvent.PendingPlaced(ev.order))
                    }
                    is TradingEngine.Event.PendingModified -> {
                        pendingDao.upsert(ev.order.toEntity())
                        _events.tryEmit(TradingEvent.PendingModified(ev.order))
                    }
                    is TradingEngine.Event.PendingCanceled -> {
                        pendingDao.delete(ev.orderId)
                        _events.tryEmit(TradingEvent.PendingCanceled(ev.orderId))
                    }
                    is TradingEngine.Event.PendingTriggered -> {
                        pendingDao.delete(ev.orderId)
                        _events.tryEmit(TradingEvent.PendingTriggered(ev.orderId, ev.openedPositionId))
                    }

                    is TradingEngine.Event.AccountUpdated -> Unit
                }
            }
        }
    }

    override fun observeTradingEvents(): Flow<TradingEvent> = eventsFlow

    override fun observeAccount(): Flow<AccountSnapshot> = engine.account

    override fun observeOpenPositions(): Flow<List<com.tradingapp.metatrader.domain.models.trading.Position>> =
        positionDao.observeAll().map { it.map { e -> e.toDomain() } }

    override fun observeHistory(): Flow<List<com.tradingapp.metatrader.domain.models.trading.ClosedTrade>> =
        closedDao.observeAll().map { it.map { e -> e.toDomain() } }

    override fun observePendingOrders(): Flow<List<PendingOrder>> =
        pendingDao.observeAll().map { it.map { e -> e.toDomain() } }

    override suspend fun placeMarketOrder(
        instrument: String,
        side: Position.Side,
        price: Double,
        lots: Double,
        sl: Double?,
        tp: Double?,
        comment: String?
    ) {
        engine.placeMarketOrder(instrument, side, Instant.now(), price, lots, sl, tp, comment)
    }

    override suspend fun placePendingOrder(
        instrument: String,
        type: PendingOrder.Type,
        targetPrice: Double,
        lots: Double,
        sl: Double?,
        tp: Double?,
        comment: String?
    ) {
        engine.placePendingOrder(instrument, type, Instant.now(), targetPrice, lots, sl, tp, comment)
    }

    override suspend fun cancelPendingOrder(orderId: String) {
        engine.cancelPending(orderId)
    }

    override suspend fun modifyPosition(positionId: String, newSl: Double?, newTp: Double?) {
        engine.modifyPosition(positionId, newSl, newTp)
    }

    override suspend fun modifyPendingOrder(orderId: String, newTarget: Double, newSl: Double?, newTp: Double?) {
        engine.modifyPending(orderId, newTarget, newSl, newTp)
    }

    override suspend fun closePosition(positionId: String, price: Double) {
        engine.closePosition(positionId, Instant.now(), price)
    }

    override fun onTick(time: Instant, bid: Double, ask: Double) {
        engine.onTick(time, bid, ask)
    }
}
