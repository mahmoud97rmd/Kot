package com.tradingapp.metatrader.domain.repository

import com.tradingapp.metatrader.domain.models.strategy.StrategySettings
import kotlinx.coroutines.flow.Flow

interface StrategySettingsRepository {
    fun observe(): Flow<StrategySettings>
    suspend fun update(transform: (StrategySettings) -> StrategySettings)
    suspend fun set(settings: StrategySettings)
}
