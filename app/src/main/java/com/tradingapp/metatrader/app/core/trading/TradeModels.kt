package com.tradingapp.metatrader.app.core.trading

enum class OrderSide { BUY, SELL }

data class MarketOrderRequest(
    val symbol: String,
    val side: OrderSide,
    val units: Long, // OANDA uses signed units: + for buy, - for sell (we will map)
    val takeProfitPrice: Double? = null,
    val stopLossPrice: Double? = null
)

data class OrderResult(
    val ok: Boolean,
    val message: String,
    val raw: String? = null
)

interface TradeExecutor {
    suspend fun placeMarketOrder(req: MarketOrderRequest): OrderResult
}
