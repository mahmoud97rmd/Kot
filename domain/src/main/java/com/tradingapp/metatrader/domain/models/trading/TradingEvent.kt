package com.tradingapp.metatrader.domain.models.trading

import com.tradingapp.metatrader.core.engine.backtest.SimpleBacktestEngine.Trade
import java.time.Instant

sealed class TradingEvent {
    data class PositionOpened(val position: Position) : TradingEvent()
    data class PositionClosed(val trade: Trade) : TradingEvent()
    data class PendingPlaced(val order: PendingOrder) : TradingEvent()

    // Important for replay markers
    data class PendingTriggered(
        val orderId: String,
        val openedPositionId: String,
        val triggerTime: Instant
    ) : TradingEvent()

    data class PositionModified(val positionId: String, val stopLoss: Double?, val takeProfit: Double?) : TradingEvent()
    data class PendingModified(val orderId: String, val targetPrice: Double, val stopLoss: Double?, val takeProfit: Double?) : TradingEvent()
}
