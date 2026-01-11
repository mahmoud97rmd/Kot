package com.tradingapp.metatrader.domain.usecases.trading

import com.tradingapp.metatrader.domain.models.trading.Position
import com.tradingapp.metatrader.domain.repository.TradingRepository

class PlaceMarketOrderUseCase(private val repo: TradingRepository) {
    suspend operator fun invoke(
        instrument: String,
        side: Position.Side,
        price: Double,
        lots: Double,
        sl: Double?,
        tp: Double?,
        comment: String?
    ) = repo.placeMarketOrder(instrument, side, price, lots, sl, tp, comment)
}
