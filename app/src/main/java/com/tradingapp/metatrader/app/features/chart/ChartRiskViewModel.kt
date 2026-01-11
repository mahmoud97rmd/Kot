package com.tradingapp.metatrader.app.features.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingapp.metatrader.domain.usecases.trading.ObservePositionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ChartRiskViewModel @Inject constructor(
    observePositions: ObservePositionsUseCase
) : ViewModel() {
    val positions = observePositions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
