package com.tradingapp.metatrader.domain.usecases.trading

import com.tradingapp.metatrader.domain.models.trading.Trade
import com.tradingapp.metatrader.domain.repository.TradingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTradeHistoryUseCase @Inject constructor(
    private val repo: TradingRepository
) {
    operator fun invoke(): Flow<List<Trade>> = repo.observeTradeHistory()
}
