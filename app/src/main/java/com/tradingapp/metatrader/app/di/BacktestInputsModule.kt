package com.tradingapp.metatrader.app.di

import com.tradingapp.metatrader.app.features.backtest.inputs.store.BacktestInputsStore
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object BacktestInputsModule {
    // BacktestInputsStore has @Inject constructor, no providers needed.
    // This module exists to keep structure consistent if you want explicit bindings later.
}
