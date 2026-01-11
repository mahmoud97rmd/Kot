package com.tradingapp.metatrader.domain.usecases.strategy

import com.tradingapp.metatrader.domain.models.strategy.StrategySettings
import com.tradingapp.metatrader.domain.repository.StrategySettingsRepository
import javax.inject.Inject

class UpdateStrategySettingsUseCase @Inject constructor(
    private val repo: StrategySettingsRepository
) {
    suspend operator fun invoke(transform: (StrategySettings) -> StrategySettings) {
        repo.update(transform)
    }

    suspend fun set(settings: StrategySettings) {
        repo.set(settings)
    }
}
