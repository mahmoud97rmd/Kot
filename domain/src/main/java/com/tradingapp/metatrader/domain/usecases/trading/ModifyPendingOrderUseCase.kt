package com.tradingapp.metatrader.domain.usecases.trading

import com.tradingapp.metatrader.domain.repository.TradingRepository

class ModifyPendingOrderUseCase(private val repo: TradingRepository) {
    suspend operator fun invoke(orderId: String, newTarget: Double, newSl: Double?, newTp: Double?) =
        repo.modifyPendingOrder(orderId, newTarget, newSl, newTp)
}
