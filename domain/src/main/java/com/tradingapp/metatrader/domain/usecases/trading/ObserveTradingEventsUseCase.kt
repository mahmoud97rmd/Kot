package com.tradingapp.metatrader.domain.usecases.trading

import com.tradingapp.metatrader.domain.repository.TradingRepository

class ObserveTradingEventsUseCase(private val repo: TradingRepository) {
    operator fun invoke() = repo.observeTradingEvents()
}
