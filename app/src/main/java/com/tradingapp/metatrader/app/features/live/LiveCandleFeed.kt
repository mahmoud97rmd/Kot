package com.tradingapp.metatrader.app.features.live

import com.tradingapp.metatrader.app.core.candles.Candle
import com.tradingapp.metatrader.app.core.feed.CandleFeed
import com.tradingapp.metatrader.app.core.feed.CandleUpdate
import com.tradingapp.metatrader.app.core.market.candle.CandleAggregator
import com.tradingapp.metatrader.app.core.oanda.OandaSettingsStore
import com.tradingapp.metatrader.app.core.time.TimeframeParser
import com.tradingapp.metatrader.app.data.local.cache.CandleCacheRepository
import com.tradingapp.metatrader.app.data.remote.oanda.http.OandaEndpoints
import com.tradingapp.metatrader.app.data.remote.oanda.http.OandaHttpClient
import com.tradingapp.metatrader.app.data.remote.oanda.rest.OandaRestClient
import com.tradingapp.metatrader.app.data.remote.oanda.stream.OandaMarketFeed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

class LiveCandleFeed(
    private val settingsStore: OandaSettingsStore,
    private val cache: CandleCacheRepository
) : CandleFeed {

    var renderCount: Int = 800
    var cacheKeep: Int = 5000

    override fun stream(symbol: String, timeframe: String): Flow<CandleUpdate> = flow {
        val tfSec = TimeframeParser.toSeconds(timeframe)
        val overlapSec = tfSec * 3

        var attempt = 0

        while (currentCoroutineContext().isActive) {
            try {
                emit(CandleUpdate.Status("Loading OANDA settings..."))
                val settings = settingsStore.getNow()
                    ?: throw IllegalStateException("OANDA not configured. Set token/account in OANDA settings.")

                val restBase = OandaEndpoints.restBaseUrl(settings.environment)
                val streamBase = OandaEndpoints.streamBaseUrl(settings.environment)

                val http = OandaHttpClient.create { settings.apiToken }
                val rest = OandaRestClient(http)

                // Cache-first
                emit(CandleUpdate.Status("Cache-first loading..."))
                val cached = withContext(Dispatchers.IO) { cache.loadRecentUnified(symbol, timeframe, renderCount) }
                if (cached.isNotEmpty()) {
                    emit(CandleUpdate.History(cached))
                    emit(CandleUpdate.Status("Cache loaded: ${cached.size}. Gap filling..."))
                } else {
                    emit(CandleUpdate.Status("No cache. Fetching history..."))
                }

                // Smart gap fill
                val lastCached = withContext(Dispatchers.IO) { cache.getLastTimeSec(symbol, timeframe) }
                val fromSec = lastCached?.let { (it - overlapSec).coerceAtLeast(0L) }

                val fetchedUnified: List<Candle> = withContext(Dispatchers.IO) {
                    if (fromSec == null) {
                        rest.getCandles(restBase, symbol, timeframe, count = cacheKeep.coerceAtMost(2000))
                    } else {
                        rest.getCandlesSince(restBase, symbol, timeframe, fromEpochSec = fromSec, count = 5000)
                    }
                }.map { mc ->
                    Candle(
                        timeSec = mc.timeSec,
                        open = mc.open,
                        high = mc.high,
                        low = mc.low,
                        close = mc.close,
                        volume = mc.volume
                    )
                }

                withContext(Dispatchers.IO) {
                    cache.upsertUnified(symbol, timeframe, fetchedUnified)
                    cache.trimKeepLast(symbol, timeframe, cacheKeep)
                }

                val ready = withContext(Dispatchers.IO) { cache.loadRecentUnified(symbol, timeframe, renderCount) }
                if (ready.isNotEmpty()) emit(CandleUpdate.History(ready))

                emit(CandleUpdate.Status("Streaming live..."))

                // Stream ticks -> CandleAggregator -> Current candle updates
                val agg = CandleAggregator(timeframe).also { it.reset() }
                val feed = OandaMarketFeed(http, streamBaseUrl = streamBase, accountId = settings.accountId)

                attempt = 0
                feed.ticks(listOf(symbol)).collect { tick ->
                    val upd = agg.onTick(tick)
                    val cur = Candle(
                        timeSec = upd.current.timeSec,
                        open = upd.current.open,
                        high = upd.current.high,
                        low = upd.current.low,
                        close = upd.current.close,
                        volume = upd.current.volume
                    )
                    emit(CandleUpdate.Current(cur))
                }

                throw IllegalStateException("Stream ended unexpectedly")
            } catch (ce: kotlinx.coroutines.CancellationException) {
                emit(CandleUpdate.Status("Disconnected."))
                return@flow
            } catch (t: Throwable) {
                attempt += 1
                val backoffMs = computeBackoffMs(attempt)
                emit(CandleUpdate.Status("Disconnected: ${t.message}. Reconnecting in ${backoffMs / 1000}s..."))
                delay(backoffMs)
            }
        }
    }

    private fun computeBackoffMs(attempt: Int): Long {
        val base = 1000L
        val max = 30_000L
        val exp = attempt.coerceIn(0, 5)
        val pow = 1L shl exp
        return (base * pow).coerceAtMost(max)
    }
}
