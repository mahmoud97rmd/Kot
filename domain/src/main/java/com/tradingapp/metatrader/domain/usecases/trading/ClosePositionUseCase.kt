package com.tradingapp.metatrader.domain.usecases.trading

import com.tradingapp.metatrader.domain.repository.TradingRepository

class ClosePositionUseCase(private val repo: TradingRepository) {
    suspend operator fun invoke(positionId: String, price: Double) =
        repo.closePosition(positionId, price)
}
