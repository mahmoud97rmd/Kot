package com.tradingapp.metatrader.domain.usecases.watchlist

import com.tradingapp.metatrader.domain.repository.WatchlistRepository

class ObserveWatchlistUseCase(private val repo: WatchlistRepository) {
    operator fun invoke() = repo.observeWatchlist()
}
