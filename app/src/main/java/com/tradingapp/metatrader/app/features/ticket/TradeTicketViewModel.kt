package com.tradingapp.metatrader.app.features.ticket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingapp.metatrader.domain.models.trading.PendingOrder
import com.tradingapp.metatrader.domain.models.trading.Position
import com.tradingapp.metatrader.domain.usecases.trading.ModifyPendingOrderUseCase
import com.tradingapp.metatrader.domain.usecases.trading.ModifyPositionUseCase
import com.tradingapp.metatrader.domain.usecases.trading.PlaceMarketOrderUseCase
import com.tradingapp.metatrader.domain.usecases.trading.PlacePendingOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TradeTicketViewModel @Inject constructor(
    private val placeMarket: PlaceMarketOrderUseCase,
    private val placePending: PlacePendingOrderUseCase,
    private val modifyPosition: ModifyPositionUseCase,
    private val modifyPending: ModifyPendingOrderUseCase
) : ViewModel() {

    private val _status = MutableStateFlow("Status: idle")
    val status: StateFlow<String> = _status.asStateFlow()

    fun submitMarket(
        instrument: String,
        side: Position.Side,
        price: Double,
        lots: Double,
        sl: Double?,
        tp: Double?
    ) {
        viewModelScope.launch {
            runCatching {
                placeMarket(instrument, side, price, lots, sl, tp, comment = "Ticket Market")
            }.onSuccess {
                _status.value = "Status: market order placed ($side $instrument)"
            }.onFailure { e ->
                _status.value = "Status: error ${e.message}"
            }
        }
    }

    fun submitPending(
        instrument: String,
        type: PendingOrder.Type,
        target: Double,
        lots: Double,
        sl: Double?,
        tp: Double?
    ) {
        viewModelScope.launch {
            runCatching {
                placePending(instrument, type, target, lots, sl, tp, comment = "Ticket Pending")
            }.onSuccess {
                _status.value = "Status: pending placed ($type $instrument @ $target)"
            }.onFailure { e ->
                _status.value = "Status: error ${e.message}"
            }
        }
    }

    fun modifyPositionRisk(positionId: String, sl: Double?, tp: Double?) {
        viewModelScope.launch {
            runCatching { modifyPosition(positionId, sl, tp) }
                .onSuccess { _status.value = "Status: position modified ($positionId)" }
                .onFailure { e -> _status.value = "Status: error ${e.message}" }
        }
    }

    fun modifyPendingOrder(orderId: String, target: Double, sl: Double?, tp: Double?) {
        viewModelScope.launch {
            runCatching { modifyPending(orderId, target, sl, tp) }
                .onSuccess { _status.value = "Status: pending modified ($orderId)" }
                .onFailure { e -> _status.value = "Status: error ${e.message}" }
        }
    }
}
