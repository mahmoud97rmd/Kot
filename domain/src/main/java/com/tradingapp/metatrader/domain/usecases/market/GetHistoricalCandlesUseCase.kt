package com.tradingapp.metatrader.domain.usecases.market

import com.tradingapp.metatrader.domain.models.Candle
import com.tradingapp.metatrader.domain.models.Timeframe
import com.tradingapp.metatrader.domain.repository.MarketRepository

class GetHistoricalCandlesUseCase(
    private val repo: MarketRepository
) {
    suspend operator fun invoke(
        instrument: String,
        timeframe: Timeframe,
        count: Int = 500
    ): List<Candle> {
        // Cache-first: اعرض من Room ثم حدّث من الشبكة
        val cached = repo.getCachedCandles(instrument, timeframe, count)
        if (cached.isNotEmpty()) return cached

        val net = repo.getHistoricalCandles(instrument, timeframe, count)
        repo.saveCandles(instrument, timeframe, net)
        return net
    }
}
