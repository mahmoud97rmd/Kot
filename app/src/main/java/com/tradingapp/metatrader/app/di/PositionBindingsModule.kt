package com.tradingapp.metatrader.app.di

import com.tradingapp.metatrader.app.core.trading.positions.PositionService
import com.tradingapp.metatrader.app.features.oanda.positions.OandaPositionService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PositionBindingsModule {

    @Binds
    @Singleton
    abstract fun bindPositionService(impl: OandaPositionService): PositionService
}
