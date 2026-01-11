package com.tradingapp.metatrader.app.features.terminal.tradinghub

import com.tradingapp.metatrader.app.core.trading.mt5sim.DealMt5
import com.tradingapp.metatrader.app.core.trading.mt5sim.PendingOrderMt5
import com.tradingapp.metatrader.app.core.trading.mt5sim.PositionMt5

data class TradingHubState(
    val positions: List<PositionMt5> = emptyList(),
    val orders: List<PendingOrderMt5> = emptyList(),
    val deals: List<DealMt5> = emptyList()
)
