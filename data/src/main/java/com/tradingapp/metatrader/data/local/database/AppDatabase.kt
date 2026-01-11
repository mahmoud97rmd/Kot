package com.tradingapp.metatrader.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tradingapp.metatrader.data.local.database.dao.CandleDao
import com.tradingapp.metatrader.data.local.database.dao.ClosedTradeDao
import com.tradingapp.metatrader.data.local.database.dao.PendingOrderDao
import com.tradingapp.metatrader.data.local.database.dao.PositionDao
import com.tradingapp.metatrader.data.local.database.dao.WatchlistDao
import com.tradingapp.metatrader.data.local.database.entities.CandleEntity
import com.tradingapp.metatrader.data.local.database.entities.ClosedTradeEntity
import com.tradingapp.metatrader.data.local.database.entities.PendingOrderEntity
import com.tradingapp.metatrader.data.local.database.entities.PositionEntity
import com.tradingapp.metatrader.data.local.database.entities.WatchlistEntity

@Database(
    entities = [
        CandleEntity::class,
        WatchlistEntity::class,
        PositionEntity::class,
        ClosedTradeEntity::class,
        PendingOrderEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun candleDao(): CandleDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun positionDao(): PositionDao
    abstract fun closedTradeDao(): ClosedTradeDao
    abstract fun pendingOrderDao(): PendingOrderDao
}
