package com.tradingapp.metatrader.domain.usecases.strategy

import com.tradingapp.metatrader.domain.models.strategy.StrategySettings
import com.tradingapp.metatrader.domain.repository.StrategySettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveStrategySettingsUseCase @Inject constructor(
    private val repo: StrategySettingsRepository
) {
    operator fun invoke(): Flow<StrategySettings> = repo.observe()
}
