package com.tradingapp.metatrader.app.features.expert.engine.shared

class ExpertSafetyGate(
    private val cooldownMs: Long = 2_000,
    private val maxPositions: Int = 1
) {
    private var lastBarOpenSec: Long = -1
    private var orderPlacedThisBar: Boolean = false
    private var lastOrderTimeMs: Long = 0
    private var openPositions: Int = 0

    fun onNewBar(barOpenSec: Long) {
        if (barOpenSec != lastBarOpenSec) {
            lastBarOpenSec = barOpenSec
            orderPlacedThisBar = false
        }
    }

    fun setOpenPositions(count: Int) {
        openPositions = count.coerceAtLeast(0)
    }

    fun canPlaceOrder(nowMs: Long): Boolean {
        if (nowMs - lastOrderTimeMs < cooldownMs) return false
        if (orderPlacedThisBar) return false
        if (openPositions >= maxPositions) return false
        return true
    }

    fun markOrderPlaced(nowMs: Long) {
        lastOrderTimeMs = nowMs
        orderPlacedThisBar = true
        openPositions += 1
    }
}
