package com.tradingapp.metatrader.app.di

import com.tradingapp.metatrader.app.data.local.cache.CandleCacheRepository
import com.tradingapp.metatrader.app.features.replay.ReplayCandleFeed
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReplayModule {

    @Provides
    @Singleton
    fun provideReplayFeed(cache: CandleCacheRepository): ReplayCandleFeed {
        return ReplayCandleFeed(cache)
    }
}
