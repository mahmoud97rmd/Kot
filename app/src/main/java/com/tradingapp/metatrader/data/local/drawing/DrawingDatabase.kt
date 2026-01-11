package com.tradingapp.metatrader.data.local.drawing

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DrawingEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DrawingDatabase : RoomDatabase() {
    abstract fun dao(): DrawingDao
}
