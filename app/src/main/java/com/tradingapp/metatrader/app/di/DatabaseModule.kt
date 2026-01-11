package com.tradingapp.metatrader.app.di

import android.content.Context
import androidx.room.Room
import com.tradingapp.metatrader.app.data.local.db.AppDatabase
import com.tradingapp.metatrader.app.data.local.db.dao.CandleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext ctx: Context): AppDatabase {
        return Room.databaseBuilder(ctx, AppDatabase::class.java, "tradingapp.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideCandleDao(db: AppDatabase): CandleDao = db.candleDao()
}
