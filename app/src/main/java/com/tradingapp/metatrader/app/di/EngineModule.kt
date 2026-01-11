package com.tradingapp.metatrader.app.di

import com.tradingapp.metatrader.core.engine.trading.TradingEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EngineModule {
    @Provides @Singleton
    fun provideTradingEngine(): TradingEngine = TradingEngine(initialBalance = 10_000.0)
}
