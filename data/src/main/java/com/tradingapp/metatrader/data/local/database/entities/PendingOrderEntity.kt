package com.tradingapp.metatrader.data.local.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pending_orders",
    indices = [Index(value = ["instrument"])]
)
data class PendingOrderEntity(
    @PrimaryKey val id: String,
    val instrument: String,
    val type: String,
    val createdAtEpochSec: Long,
    val targetPrice: Double,
    val lots: Double,
    val stopLoss: Double?,
    val takeProfit: Double?,
    val comment: String?
)
