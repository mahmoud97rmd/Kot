package com.tradingapp.metatrader.domain.backtest

import com.tradingapp.metatrader.domain.models.backtest.BacktestCandle
import com.tradingapp.metatrader.domain.models.backtest.BacktestSignal

/**
 * Strategy receives the closed candle and returns an optional signal.
 * It must be deterministic for backtest correctness.
 */
interface BacktestStrategy {
    fun onCandleClosed(history: List<BacktestCandle>): BacktestSignal?
}
