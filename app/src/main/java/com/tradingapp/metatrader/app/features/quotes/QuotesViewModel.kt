package com.tradingapp.metatrader.app.features.quotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingapp.metatrader.domain.models.market.WatchlistItem
import com.tradingapp.metatrader.domain.usecases.watchlist.AddWatchlistItemUseCase
import com.tradingapp.metatrader.domain.usecases.watchlist.ObserveWatchlistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuotesViewModel @Inject constructor(
    observe: ObserveWatchlistUseCase,
    private val add: AddWatchlistItemUseCase
) : ViewModel() {

    val watchlist = observe().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun seedIfEmpty() {
        viewModelScope.launch {
            if (watchlist.value.isEmpty()) {
                add(WatchlistItem("XAU_USD", "Gold"))
                add(WatchlistItem("EUR_USD", "EURUSD"))
            }
        }
    }
}
