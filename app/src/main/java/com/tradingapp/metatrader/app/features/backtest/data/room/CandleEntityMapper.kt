package com.tradingapp.metatrader.app.features.backtest.data.room

import com.tradingapp.metatrader.data.local.backtestdb.entities.CandleEntity
import com.tradingapp.metatrader.domain.models.backtest.BacktestCandle

object CandleEntityMapper {
    fun toDomain(e: CandleEntity): BacktestCandle {
        return BacktestCandle(
            timeSec = e.timeSec,
            open = e.open,
            high = e.high,
            low = e.low,
            close = e.close
        )
    }
}
