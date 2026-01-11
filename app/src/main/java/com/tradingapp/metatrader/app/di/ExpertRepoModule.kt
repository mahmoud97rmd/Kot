package com.tradingapp.metatrader.app.di

import android.content.Context
import com.tradingapp.metatrader.app.features.expert.data.ExpertScriptRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExpertRepoModule {

    @Provides
    @Singleton
    fun provideExpertScriptRepo(@ApplicationContext ctx: Context): ExpertScriptRepository {
        return ExpertScriptRepository(ctx)
    }
}
