package com.tradingapp.metatrader.domain.usecases.watchlist

import com.tradingapp.metatrader.domain.models.market.WatchlistItem
import com.tradingapp.metatrader.domain.repository.WatchlistRepository

class AddWatchlistItemUseCase(private val repo: WatchlistRepository) {
    suspend operator fun invoke(item: WatchlistItem) = repo.addItem(item)
}
