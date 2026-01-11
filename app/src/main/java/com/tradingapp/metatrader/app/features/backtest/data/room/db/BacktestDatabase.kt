package com.tradingapp.metatrader.app.features.backtest.data.room.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tradingapp.metatrader.app.features.backtest.data.room.dao.BacktestCandleDao
import com.tradingapp.metatrader.app.features.backtest.data.room.entities.BacktestCandleEntity

@Database(
    entities = [BacktestCandleEntity::class],
    version = 1,
    exportSchema = true
)
abstract class BacktestDatabase : RoomDatabase() {
    abstract fun candleDao(): BacktestCandleDao
}
