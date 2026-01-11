package com.tradingapp.metatrader.domain.repository

import com.tradingapp.metatrader.domain.models.market.WatchlistItem
import kotlinx.coroutines.flow.Flow

interface WatchlistRepository {
    fun observeWatchlist(): Flow<List<WatchlistItem>>
    suspend fun addItem(item: WatchlistItem)
    suspend fun removeInstrument(instrument: String)
}
