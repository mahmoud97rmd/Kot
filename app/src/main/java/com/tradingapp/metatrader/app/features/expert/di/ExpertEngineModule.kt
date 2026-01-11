package com.tradingapp.metatrader.app.features.expert.di

import com.tradingapp.metatrader.app.features.expert.engine.backtest.ExpertBacktestRunner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExpertEngineModule {

    @Provides
    @Singleton
    fun provideExpertBacktestRunner(): ExpertBacktestRunner = ExpertBacktestRunner()
}
