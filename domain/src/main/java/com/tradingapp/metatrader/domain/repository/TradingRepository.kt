package com.tradingapp.metatrader.domain.repository

import com.tradingapp.metatrader.domain.models.trading.AccountSnapshot
import com.tradingapp.metatrader.domain.models.trading.ClosedTrade
import com.tradingapp.metatrader.domain.models.trading.PendingOrder
import com.tradingapp.metatrader.domain.models.trading.Position
import com.tradingapp.metatrader.domain.models.trading.TradingEvent
import kotlinx.coroutines.flow.Flow

interface TradingRepository {
    fun observeAccount(): Flow<AccountSnapshot>
    fun observeOpenPositions(): Flow<List<Position>>
    fun observeHistory(): Flow<List<ClosedTrade>>
    fun observePendingOrders(): Flow<List<PendingOrder>>

    fun observeTradingEvents(): Flow<TradingEvent>

    suspend fun placeMarketOrder(
        instrument: String,
        side: Position.Side,
        price: Double,
        lots: Double,
        sl: Double?,
        tp: Double?,
        comment: String?
    )

    suspend fun placePendingOrder(
        instrument: String,
        type: PendingOrder.Type,
        targetPrice: Double,
        lots: Double,
        sl: Double?,
        tp: Double?,
        comment: String?
    )

    suspend fun cancelPendingOrder(orderId: String)

    suspend fun modifyPosition(positionId: String, newSl: Double?, newTp: Double?)
    suspend fun modifyPendingOrder(orderId: String, newTarget: Double, newSl: Double?, newTp: Double?)

    suspend fun closePosition(positionId: String, price: Double)
}
