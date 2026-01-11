package com.tradingapp.metatrader.app.features.trade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingapp.metatrader.domain.models.trading.PendingOrder
import com.tradingapp.metatrader.domain.models.trading.Position
import com.tradingapp.metatrader.domain.usecases.trading.CancelPendingOrderUseCase
import com.tradingapp.metatrader.domain.usecases.trading.ObservePendingOrdersUseCase
import com.tradingapp.metatrader.domain.usecases.trading.PlaceMarketOrderUseCase
import com.tradingapp.metatrader.domain.usecases.trading.PlacePendingOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TradeViewModel @Inject constructor(
    private val placeMarket: PlaceMarketOrderUseCase,
    private val placePending: PlacePendingOrderUseCase,
    private val cancelPending: CancelPendingOrderUseCase,
    observePending: ObservePendingOrdersUseCase
) : ViewModel() {

    private val _status = MutableStateFlow("Status: idle")
    val status: StateFlow<String> = _status.asStateFlow()

    val pendingOrders = observePending()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun buyMarket(instrument: String, price: Double, lots: Double, sl: Double?, tp: Double?) {
        submitMarket(instrument, Position.Side.BUY, price, lots, sl, tp)
    }

    fun sellMarket(instrument: String, price: Double, lots: Double, sl: Double?, tp: Double?) {
        submitMarket(instrument, Position.Side.SELL, price, lots, sl, tp)
    }

    private fun submitMarket(instrument: String, side: Position.Side, price: Double, lots: Double, sl: Double?, tp: Double?) {
        viewModelScope.launch {
            runCatching {
                placeMarket(instrument, side, price, lots, sl, tp, comment = "Manual Market")
            }.onSuccess {
                _status.value = "Status: market order sent ($side $instrument)"
            }.onFailure { e ->
                _status.value = "Status: error ${e.message}"
            }
        }
    }

    fun placePendingOrder(
        instrument: String,
        type: PendingOrder.Type,
        targetPrice: Double,
        lots: Double,
        sl: Double?,
        tp: Double?
    ) {
        viewModelScope.launch {
            runCatching {
                placePending(instrument, type, targetPrice, lots, sl, tp, comment = "Manual Pending")
            }.onSuccess {
                _status.value = "Status: pending placed ($type $instrument @ $targetPrice)"
            }.onFailure { e ->
                _status.value = "Status: error ${e.message}"
            }
        }
    }

    fun cancel(orderId: String) {
        viewModelScope.launch {
            runCatching { cancelPending(orderId) }
                .onSuccess { _status.value = "Status: pending canceled ($orderId)" }
                .onFailure { e -> _status.value = "Status: error ${e.message}" }
        }
    }
}
