package com.tradingapp.metatrader.domain.repository

import java.time.Instant

interface TradingEngineInput {
    fun onTick(time: Instant, bid: Double, ask: Double)
}
