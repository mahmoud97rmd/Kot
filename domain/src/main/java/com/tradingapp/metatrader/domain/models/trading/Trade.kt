package com.tradingapp.metatrader.domain.models.trading

import java.time.Instant

/**
 * Domain Trade model.
 * Keep it simple so domain can compile independently of UI/core engine internals.
 */
data class Trade(
    val id: String,
    val instrument: String,
    val openedAt: Instant,
    val closedAt: Instant? = null,
    val volume: Double = 0.0,
    val profit: Double = 0.0,
    val openPrice: Double = 0.0,
    val closePrice: Double = 0.0
)
