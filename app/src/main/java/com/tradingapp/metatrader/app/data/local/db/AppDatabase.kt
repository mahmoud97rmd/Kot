package com.tradingapp.metatrader.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tradingapp.metatrader.app.data.local.db.dao.CandleDao
import com.tradingapp.metatrader.app.data.local.db.entities.CandleEntity

@Database(
    entities = [CandleEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun candleDao(): CandleDao
}
