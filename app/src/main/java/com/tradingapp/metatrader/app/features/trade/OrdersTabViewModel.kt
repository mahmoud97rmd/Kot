package com.tradingapp.metatrader.app.features.trade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingapp.metatrader.domain.usecases.trading.CancelPendingOrderUseCase
import com.tradingapp.metatrader.domain.usecases.trading.ModifyPendingOrderUseCase
import com.tradingapp.metatrader.domain.usecases.trading.ObservePendingOrdersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersTabViewModel @Inject constructor(
    observePending: ObservePendingOrdersUseCase,
    private val cancel: CancelPendingOrderUseCase,
    private val modify: ModifyPendingOrderUseCase
) : ViewModel() {

    val orders = observePending()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun cancel(orderId: String) {
        viewModelScope.launch { cancel(orderId) }
    }

    fun modify(orderId: String, newTarget: Double, newSl: Double?, newTp: Double?) {
        viewModelScope.launch { modify(orderId, newTarget, newSl, newTp) }
    }
}
