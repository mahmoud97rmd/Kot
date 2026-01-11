package com.tradingapp.metatrader.app.core.trading.commands

import com.tradingapp.metatrader.app.core.trading.mt5sim.PendingType

sealed class OrderCommand {
    abstract val symbol: String

    data class PlacePending(
        override val symbol: String,
        val timeframe: String,
        val type: PendingType,
        val lots: Double,
        val entryPrice: Double,
        val sl: Double?,
        val tp: Double?,
        val comment: String?
    ) : OrderCommand()

    data class CancelPending(
        override val symbol: String,
        val timeframe: String,
        val orderId: String
    ) : OrderCommand()

    data class ModifyPositionStops(
        override val symbol: String,
        val positionId: String,
        val newSl: Double?,
        val newTp: Double?
    ) : OrderCommand()

    data class ClosePartial(
        override val symbol: String,
        val positionId: String,
        val closeLots: Double
    ) : OrderCommand()
}
