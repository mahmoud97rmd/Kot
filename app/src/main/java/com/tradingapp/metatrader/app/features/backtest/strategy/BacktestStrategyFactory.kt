package com.tradingapp.metatrader.app.features.backtest.strategy

import com.tradingapp.metatrader.app.features.backtest.inputs.BacktestInputs
import com.tradingapp.metatrader.domain.backtest.BacktestStrategy
import com.tradingapp.metatrader.domain.backtest.RsiReversalStrategy
import com.tradingapp.metatrader.domain.backtest.SimpleEmaCrossStrategy
import com.tradingapp.metatrader.domain.backtest.StochasticCrossStrategy

object BacktestStrategyFactory {

    fun create(inputs: BacktestInputs): BacktestStrategy {
        return when (inputs.strategyType) {
            StrategyType.EMA_CROSS -> SimpleEmaCrossStrategy(
                fast = inputs.emaFast,
                slow = inputs.emaSlow,
                lots = inputs.lots
            )

            StrategyType.RSI_REVERSAL -> RsiReversalStrategy(
                period = inputs.rsiPeriod,
                oversold = inputs.rsiOversold,
                overbought = inputs.rsiOverbought,
                lots = inputs.lots
            )

            StrategyType.STOCH_CROSS -> StochasticCrossStrategy(
                kPeriod = inputs.stochK,
                dPeriod = inputs.stochD,
                oversold = inputs.stochOversold,
                overbought = inputs.stochOverbought,
                lots = inputs.lots
            )
        }
    }
}
