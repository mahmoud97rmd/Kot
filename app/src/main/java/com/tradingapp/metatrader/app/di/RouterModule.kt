package com.tradingapp.metatrader.app.di

import com.tradingapp.metatrader.app.state.TradingTickRouter
import com.tradingapp.metatrader.domain.repository.TradingEngineInput
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RouterModule {

    @Provides
    @Singleton
    fun provideTradingTickRouter(input: TradingEngineInput): TradingTickRouter = TradingTickRouter(input)
}
