package com.tradingapp.metatrader.app.features.terminal.tradinghub

import com.tradingapp.metatrader.app.core.trading.mt5sim.DealMt5
import com.tradingapp.metatrader.app.core.trading.mt5sim.PendingOrderMt5
import com.tradingapp.metatrader.app.core.trading.mt5sim.PositionMt5
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TradingHub @Inject constructor() {

    private val _state = MutableStateFlow(TradingHubState())
    val state: StateFlow<TradingHubState> = _state.asStateFlow()

    @Synchronized
    fun updateFromSession(
        positions: List<PositionMt5>,
        orders: List<PendingOrderMt5>,
        dealsDelta: List<DealMt5>
    ) {
        val cur = _state.value
        val newDeals = (cur.deals + dealsDelta).takeLast(10_000)
        _state.value = cur.copy(
            positions = positions,
            orders = orders,
            deals = newDeals
        )
    }

    @Synchronized
    fun clear() {
        _state.value = TradingHubState()
    }
}
