package com.tradingapp.metatrader.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val instrument: String,
    val displayName: String
)
