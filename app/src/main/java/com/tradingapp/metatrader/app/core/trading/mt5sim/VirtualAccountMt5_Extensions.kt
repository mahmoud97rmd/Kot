package com.tradingapp.metatrader.app.core.trading.mt5sim

/**
 * Helper storage for last quote (per session usage).
 * Keep it out of VirtualAccountMt5 core if you don't want state pollution.
 */
class QuoteBook {
    private val map = HashMap<String, PriceQuote>()
    fun set(symbol: String, q: PriceQuote) { map[symbol] = q }
    fun get(symbol: String): PriceQuote? = map[symbol]
}

fun VirtualAccountMt5.modifyStops(positionId: String, newSl: Double?, newTp: Double?): Boolean {
    val idx = positions.indexOfFirst { it.id == positionId }
    if (idx < 0) return false
    val p = positions[idx]
    positions[idx] = p.copy(stopLoss = newSl, takeProfit = newTp)
    return true
}
