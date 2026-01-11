package com.tradingapp.metatrader.domain.usecases.trading

import com.tradingapp.metatrader.domain.models.trading.PendingOrder
import com.tradingapp.metatrader.domain.repository.TradingRepository

class PlacePendingOrderUseCase(private val repo: TradingRepository) {
    suspend operator fun invoke(
        instrument: String,
        type: PendingOrder.Type,
        targetPrice: Double,
        lots: Double,
        sl: Double?,
        tp: Double?,
        comment: String?
    ) = repo.placePendingOrder(instrument, type, targetPrice, lots, sl, tp, comment)
}
