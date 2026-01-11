package com.tradingapp.metatrader.app.di

import com.tradingapp.metatrader.app.features.backtest.data.oanda.gaps.GapAnalyzer
import com.tradingapp.metatrader.app.features.backtest.data.room.BacktestCandleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BacktestOandaToolsModule {

    @Provides
    @Singleton
    fun provideGapAnalyzer(repo: BacktestCandleRepository): GapAnalyzer =
        GapAnalyzer(repo)
}
