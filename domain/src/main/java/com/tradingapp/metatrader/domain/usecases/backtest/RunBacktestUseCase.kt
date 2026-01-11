package com.tradingapp.metatrader.domain.usecases.backtest

import com.tradingapp.metatrader.domain.backtest.BacktestEngine
import com.tradingapp.metatrader.domain.backtest.BacktestStrategy
import com.tradingapp.metatrader.domain.models.backtest.BacktestCandle
import com.tradingapp.metatrader.domain.models.backtest.BacktestConfig
import com.tradingapp.metatrader.domain.models.backtest.BacktestResult
import javax.inject.Inject

class RunBacktestUseCase @Inject constructor() {

    operator fun invoke(
        candles: List<BacktestCandle>,
        strategy: BacktestStrategy,
        config: BacktestConfig,
        onProgress: ((BacktestEngine.Progress) -> Unit)? = null
    ): BacktestResult {
        return engine.run(
            config = config,
            candles = candles,
            strategy = strategy,
            onProgress = onProgress
        )
    }
}
