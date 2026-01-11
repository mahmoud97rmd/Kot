package com.tradingapp.metatrader.app.di

import android.content.Context
import com.tradingapp.metatrader.data.preferences.StrategySettingsStore
import com.tradingapp.metatrader.domain.repository.StrategySettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StrategySettingsModule {

    @Provides
    @Singleton
    fun provideStrategySettingsRepository(
        @ApplicationContext context: Context
    ): StrategySettingsRepository = StrategySettingsStore(context)
}
