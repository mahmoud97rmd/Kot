package com.tradingapp.metatrader.data.repository

import com.tradingapp.metatrader.data.local.database.dao.WatchlistDao
import com.tradingapp.metatrader.data.mappers.toDomain
import com.tradingapp.metatrader.data.mappers.toEntity
import com.tradingapp.metatrader.domain.models.market.WatchlistItem
import com.tradingapp.metatrader.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WatchlistRepositoryImpl(
    private val dao: WatchlistDao
) : WatchlistRepository {

    override fun observeWatchlist(): Flow<List<WatchlistItem>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun addItem(item: WatchlistItem) {
        dao.upsert(item.toEntity())
    }

    override suspend fun removeInstrument(instrument: String) {
        dao.delete(instrument)
    }
}
