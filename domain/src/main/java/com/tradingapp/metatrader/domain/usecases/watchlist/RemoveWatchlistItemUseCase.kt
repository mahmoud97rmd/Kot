package com.tradingapp.metatrader.domain.usecases.watchlist

import com.tradingapp.metatrader.domain.repository.WatchlistRepository

class RemoveWatchlistItemUseCase(private val repo: WatchlistRepository) {
    suspend operator fun invoke(instrument: String) = repo.removeInstrument(instrument)
}
