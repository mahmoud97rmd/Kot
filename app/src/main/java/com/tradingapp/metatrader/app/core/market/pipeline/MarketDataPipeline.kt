package com.tradingapp.metatrader.app.core.market.pipeline

import com.tradingapp.metatrader.app.core.market.MarketCandle
import com.tradingapp.metatrader.app.core.market.MarketTick
import com.tradingapp.metatrader.app.core.market.candle.CandleAggregator
import com.tradingapp.metatrader.app.core.oanda.OandaSettings
import com.tradingapp.metatrader.app.core.oanda.OandaSettingsStore
import com.tradingapp.metatrader.app.data.remote.oanda.http.OandaEndpoints
import com.tradingapp.metatrader.app.data.remote.oanda.http.OandaHttpClient
import com.tradingapp.metatrader.app.data.remote.oanda.rest.OandaRestClient
import com.tradingapp.metatrader.app.data.remote.oanda.stream.OandaMarketFeed
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

class MarketDataPipeline(
    private val settingsStore: OandaSettingsStore
) {

    data class Callbacks(
        val onStatus: (String) -> Unit,
        val onHistory: (List<MarketCandle>) -> Unit,
        val onLiveCandle: (MarketCandle) -> Unit,
        val onClosedCandle: (MarketCandle) -> Unit = {}
    )

    private var job: Job? = null

    suspend fun connect(
        symbol: String,
        timeframe: String,
        callbacks: Callbacks
    ) {
        callbacks.onStatus("Loading OANDA settings...")
        val settings = settingsStore.getNow()
            ?: throw IllegalStateException("OANDA not configured. Set token/account in OANDA settings.")

        callbacks.onStatus("Preparing clients (${settings.environment})...")
        val token = settings.apiToken
        val http = createHttp(token)

        val restBase = OandaEndpoints.restBaseUrl(settings.environment)
        val streamBase = OandaEndpoints.streamBaseUrl(settings.environment)

        val rest = OandaRestClient(http)
        val feed = OandaMarketFeed(http, streamBaseUrl = streamBase, accountId = settings.accountId)

        callbacks.onStatus("Fetching history: $symbol $timeframe ...")
        val history = withContext(Dispatchers.IO) {
            rest.getCandles(
                envBaseUrl = restBase,
                instrument = symbol,
                granularity = timeframe,
                count = 500
            )
        }
        callbacks.onHistory(history)
        callbacks.onStatus("History loaded: ${history.size} candles")

        val agg = CandleAggregator(timeframe)
        agg.reset()

        callbacks.onStatus("Streaming prices (live)...")
        // collect stream ticks and update candle
        feed.ticks(listOf(symbol)).collectLatest { tick ->
            onTick(agg, tick, callbacks)
        }
    }

    fun cancel() {
        job?.cancel()
        job = null
    }

    private fun createHttp(token: String): OkHttpClient {
        return OandaHttpClient.create { token }
    }

    private fun onTick(agg: CandleAggregator, t: MarketTick, cb: Callbacks) {
        val upd = agg.onTick(t)
        upd.closed?.let { cb.onClosedCandle(it) }
        cb.onLiveCandle(upd.current)
    }
}
