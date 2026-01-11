package com.tradingapp.metatrader.domain.usecases.trading

import com.tradingapp.metatrader.domain.repository.TradingRepository

class ModifyPositionUseCase(private val repo: TradingRepository) {
    suspend operator fun invoke(positionId: String, newSl: Double?, newTp: Double?) =
        repo.modifyPosition(positionId, newSl, newTp)
}
