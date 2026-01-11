package com.tradingapp.metatrader.domain.usecases.market

import com.tradingapp.metatrader.domain.models.Tick
import com.tradingapp.metatrader.domain.repository.MarketRepository
import kotlinx.coroutines.flow.Flow

class StreamTicksUseCase(
    private val repo: MarketRepository
) {
    operator fun invoke(instrument: String): Flow<Tick> = repo.streamTicks(instrument)
}
