package com.tradingapp.metatrader.app.di

import com.tradingapp.metatrader.app.features.backtest.data.room.BacktestCandleRepository
import com.tradingapp.metatrader.app.features.backtest.data.room.RoomBacktestCandleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BacktestRepoModule {

    @Binds
    @Singleton
    abstract fun bindBacktestCandleRepository(
        impl: RoomBacktestCandleRepository
    ): BacktestCandleRepository
}
