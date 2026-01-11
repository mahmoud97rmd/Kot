package com.tradingapp.metatrader.app.core.candles

import com.tradingapp.metatrader.app.core.market.MarketCandle
import com.tradingapp.metatrader.domain.models.backtest.BacktestCandle

object CandleMappers {

    fun fromMarket(c: MarketCandle): Candle = Candle(
        timeSec = c.timeSec,
        open = c.open,
        high = c.high,
        low = c.low,
        close = c.close,
        volume = c.volume
    )

    fun toMarket(c: Candle): MarketCandle = MarketCandle(
        timeSec = c.timeSec,
        open = c.open,
        high = c.high,
        low = c.low,
        close = c.close,
        volume = c.volume
    )

    fun fromBacktest(c: BacktestCandle): Candle = Candle(
        timeSec = c.timeSec,
        open = c.open,
        high = c.high,
        low = c.low,
        close = c.close,
        volume = c.volume
    )

    fun toBacktest(c: Candle): BacktestCandle = BacktestCandle(
        timeSec = c.timeSec,
        open = c.open,
        high = c.high,
        low = c.low,
        close = c.close,
        volume = c.volume
    )
}
