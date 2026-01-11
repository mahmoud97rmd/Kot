package com.tradingapp.metatrader.domain.usecases.trading

import com.tradingapp.metatrader.domain.repository.TradingRepository

class ObservePendingOrdersUseCase(private val repo: TradingRepository) {
    operator fun invoke() = repo.observePendingOrders()
}
