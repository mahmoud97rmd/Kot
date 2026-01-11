package com.tradingapp.metatrader.app.features.replay

import com.tradingapp.metatrader.app.core.candles.Candle
import com.tradingapp.metatrader.app.core.feed.CandleFeed
import com.tradingapp.metatrader.app.core.feed.CandleUpdate
import com.tradingapp.metatrader.app.data.local.cache.CandleCacheRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ReplayCandleFeed @Inject constructor(
    private val cache: CandleCacheRepository
) : CandleFeed {

    var speed: ReplaySpeed = ReplaySpeed.X4
    var baseDelayMs: Long = 250L  // visual step delay at x1

    var renderCount: Int = 800

    override fun stream(symbol: String, timeframe: String): Flow<CandleUpdate> = flow {
        emit(CandleUpdate.Status("Replay: loading candles from cache..."))
        val candles: List<Candle> = cache.loadRecentUnified(symbol, timeframe, renderCount)
        if (candles.isEmpty()) {
            emit(CandleUpdate.Status("Replay: cache is empty. Connect live first to fill cache."))
            return@flow
        }

        emit(CandleUpdate.History(candles))
        emit(CandleUpdate.Status("Replay: ready (${candles.size} candles). Speed=${speed.name}"))

        val delayMs = (baseDelayMs.toDouble() / speed.multiplier.toDouble()).toLong().coerceAtLeast(10L)

        for (c in candles) {
            emit(CandleUpdate.Current(c))
            delay(delayMs)
        }

        emit(CandleUpdate.Status("Replay finished."))
    }
}
