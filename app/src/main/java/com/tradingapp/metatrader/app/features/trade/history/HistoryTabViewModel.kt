package com.tradingapp.metatrader.app.features.trade.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingapp.metatrader.domain.usecases.trading.ObserveTradeHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistoryTabViewModel @Inject constructor(
    observeHistory: ObserveTradeHistoryUseCase
) : ViewModel() {
    val history = observeHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
