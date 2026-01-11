package com.tradingapp.metatrader.app.features.trade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingapp.metatrader.domain.usecases.trading.ClosePositionUseCase
import com.tradingapp.metatrader.domain.usecases.trading.ModifyPositionUseCase
import com.tradingapp.metatrader.domain.usecases.trading.ObserveAccountUseCase
import com.tradingapp.metatrader.domain.usecases.trading.ObservePositionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PositionsTabViewModel @Inject constructor(
    observePositions: ObservePositionsUseCase,
    observeAccount: ObserveAccountUseCase,
    private val closePos: ClosePositionUseCase,
    private val modifyPos: ModifyPositionUseCase
) : ViewModel() {

    val positions = observePositions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val account = observeAccount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun close(positionId: String, price: Double) {
        viewModelScope.launch { closePos(positionId, price) }
    }

    fun modify(positionId: String, sl: Double?, tp: Double?) {
        viewModelScope.launch { modifyPos(positionId, sl, tp) }
    }
}
