package com.tradingapp.metatrader.app.features.backtest.feed

import com.tradingapp.metatrader.app.core.candles.Candle
import com.tradingapp.metatrader.app.core.candles.CandleMappers
import com.tradingapp.metatrader.app.core.feed.CandleFeed
import com.tradingapp.metatrader.app.core.feed.CandleUpdate
import com.tradingapp.metatrader.domain.models.backtest.BacktestCandle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BacktestCandleFeed(
    private val candles: List<BacktestCandle>
) : CandleFeed {

    override fun stream(symbol: String, timeframe: String): Flow<CandleUpdate> = flow {
        val list: List<Candle> = candles.map { CandleMappers.fromBacktest(it) }
        emit(CandleUpdate.Status("Backtest feed: ${list.size} candles"))
        emit(CandleUpdate.History(list))
        // In backtest we typically iterate without delay (fast).
        for (c in list) {
            emit(CandleUpdate.Current(c))
        }
        emit(CandleUpdate.Status("Backtest feed finished"))
    }
}
