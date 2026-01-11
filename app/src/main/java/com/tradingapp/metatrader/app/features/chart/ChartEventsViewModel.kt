package com.tradingapp.metatrader.app.features.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingapp.metatrader.domain.models.trading.TradingEvent
import com.tradingapp.metatrader.domain.usecases.trading.ObserveTradingEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ChartEventsViewModel @Inject constructor(
    observeEvents: ObserveTradingEventsUseCase
) : ViewModel() {
    val events = observeEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
