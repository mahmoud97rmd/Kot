package com.tradingapp.metatrader.app.core.market.feed

import com.tradingapp.metatrader.app.core.market.MarketTick
import kotlinx.coroutines.flow.Flow

interface MarketFeed {
    fun ticks(instruments: List<String>): Flow<MarketTick>
}
