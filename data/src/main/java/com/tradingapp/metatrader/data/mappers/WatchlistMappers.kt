package com.tradingapp.metatrader.data.mappers

import com.tradingapp.metatrader.data.local.database.entities.WatchlistEntity
import com.tradingapp.metatrader.domain.models.market.WatchlistItem

fun WatchlistEntity.toDomain() = WatchlistItem(instrument = instrument, displayName = displayName)
fun WatchlistItem.toEntity() = WatchlistEntity(instrument = instrument, displayName = displayName)
