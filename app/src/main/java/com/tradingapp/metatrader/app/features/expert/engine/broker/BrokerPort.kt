package com.tradingapp.metatrader.app.features.expert.engine.broker

import com.tradingapp.metatrader.domain.models.backtest.BacktestSide

/**
 * Port abstracting broker operations needed by ExpertTradingApi.
 * This can be implemented by:
 * - SimpleSimBroker (compile-safe, works now)
 * - VirtualExchangeAdapter (wrap your existing engine)
 * - OandaLiveBroker (real trading later)
 */
interface BrokerPort {

    data class Position(
        val id: String,
        val side: BacktestSide,
        val lots: Double,
        val entryTimeSec: Long,
        val entryPrice: Double,
        val stopLoss: Double?,
        val takeProfit: Double?,
        val comment: String?
    )

    data class CloseResult(
        val positionId: String,
        val exitTimeSec: Long,
        val exitPrice: Double,
        val profit: Double
    )

    fun positions(): List<Position>

    fun openMarket(
        side: BacktestSide,
        lots: Double,
        price: Double,
        timeSec: Long,
        sl: Double?,
        tp: Double?,
        comment: String?
    ): String

    fun close(positionId: String, price: Double, timeSec: Long): CloseResult?

    /**
     * Called on each new price (tick or bar) to auto-close positions on SL/TP.
     * Returns list of close results (may be empty).
     */
    fun updateOnPrice(timeSec: Long, bid: Double, ask: Double): List<CloseResult>

    fun equity(): Double
    fun balance(): Double
}
