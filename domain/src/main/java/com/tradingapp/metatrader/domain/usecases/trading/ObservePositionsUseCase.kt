package com.tradingapp.metatrader.domain.usecases.trading

import com.tradingapp.metatrader.domain.repository.TradingRepository

class ObservePositionsUseCase(private val repo: TradingRepository) {
    operator fun invoke() = repo.observeOpenPositions()
}
