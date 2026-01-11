package com.tradingapp.metatrader.app.features.expert.runtime.api

import com.tradingapp.metatrader.domain.models.backtest.BacktestSide

interface ExpertTradingApi {
    fun nowSec(): Long

    fun symbol(): String
    fun timeframe(): String

    fun lastBid(): Double
    fun lastAsk(): Double

    fun positionsTotal(): Int

    /**
     * يرجع positionId (String) أو يرمي Exception عند الفشل.
     */
    fun orderSend(
        side: BacktestSide,
        lots: Double,
        sl: Double?,
        tp: Double?,
        comment: String?
    ): String

    /**
     * إغلاق صفقة حسب id.
     */
    fun positionClose(positionId: String): Boolean

    /**
     * Logging مثل تبويب Experts في MT5.
     */
    fun log(level: String, message: String)
}
