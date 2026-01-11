package com.tradingapp.metatrader.domain.usecases.trading

import com.tradingapp.metatrader.domain.repository.TradingRepository

class CancelPendingOrderUseCase(private val repo: TradingRepository) {
    suspend operator fun invoke(orderId: String) = repo.cancelPendingOrder(orderId)
}
