package com.tradingapp.metatrader.app.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingapp.metatrader.domain.usecases.trading.ObserveAccountUseCase
import com.tradingapp.metatrader.domain.usecases.trading.ObserveHistoryUseCase
import com.tradingapp.metatrader.domain.usecases.trading.ObservePositionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    observeAccount: ObserveAccountUseCase,
    observePositions: ObservePositionsUseCase,
    observeHistory: ObserveHistoryUseCase
) : ViewModel() {

    val account = observeAccount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val positions = observePositions().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val history = observeHistory().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
