package com.tradingapp.metatrader.app.di

import android.content.Context
import androidx.room.Room
import com.tradingapp.metatrader.app.features.backtest.data.room.BacktestCandleRepository
import com.tradingapp.metatrader.app.features.backtest.data.room.dao.BacktestCandleDao
import com.tradingapp.metatrader.app.features.backtest.data.room.db.BacktestDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BacktestRoomModule {

    @Provides
    @Singleton
    fun provideBacktestDb(@ApplicationContext ctx: Context): BacktestDatabase {
        return Room.databaseBuilder(ctx, BacktestDatabase::class.java, "backtest.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideBacktestCandleDao(db: BacktestDatabase): BacktestCandleDao = db.candleDao()

    @Provides
    @Singleton
    fun provideBacktestRepo(dao: BacktestCandleDao): BacktestCandleRepository =
        BacktestCandleRepository(dao)
}
