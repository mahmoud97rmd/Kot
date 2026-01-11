package com.tradingapp.metatrader.domain.backtest

import com.tradingapp.metatrader.domain.models.backtest.BacktestCandle
import com.tradingapp.metatrader.domain.models.backtest.BacktestConfig
import com.tradingapp.metatrader.domain.models.backtest.BacktestResult

/**
 * Domain abstraction. Implementation should live in core/data layer.
 */
interface BacktestEngine {

    data class Progress(
        val current: Int,
        val total: Int
    )

    fun run(
        candles: List<BacktestCandle>,
        strategy: BacktestStrategy,
        config: BacktestConfig,
        onProgress: ((Progress) -> Unit)? = null
    ): BacktestResult
}
