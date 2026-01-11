package com.tradingapp.metatrader.app.features.expert.engine.broker

import com.tradingapp.metatrader.app.features.expert.runtime.api.ExpertTradingApi
import com.tradingapp.metatrader.domain.models.backtest.BacktestSide

class BrokerTradingApiAdapter(
    private val broker: BrokerPort,
    private val symbol: String,
    private val timeframe: String,
    private val logger: (String, String) -> Unit,
    private val market: MarketSnapshotProvider
) : ExpertTradingApi {

    interface MarketSnapshotProvider {
        fun nowSec(): Long
        fun bid(): Double
        fun ask(): Double
    }

    override fun nowSec(): Long = market.nowSec()
    override fun symbol(): String = symbol
    override fun timeframe(): String = timeframe

    override fun lastBid(): Double = market.bid()
    override fun lastAsk(): Double = market.ask()

    override fun positionsTotal(): Int = broker.positions().size

    override fun orderSend(side: BacktestSide, lots: Double, sl: Double?, tp: Double?, comment: String?): String {
        val price = if (side == BacktestSide.BUY) market.ask() else market.bid()
        return broker.openMarket(
            side = side,
            lots = lots,
            price = price,
            timeSec = market.nowSec(),
            sl = sl,
            tp = tp,
            comment = comment
        )
    }

    override fun positionClose(positionId: String): Boolean {
        val pos = broker.positions().firstOrNull { it.id == positionId } ?: return false
        val price = if (pos.side == BacktestSide.BUY) market.bid() else market.ask()
        return broker.close(positionId, price, market.nowSec()) != null
    }

    override fun log(level: String, message: String) {
        logger(level, message)
    }
}
