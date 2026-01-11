package com.tradingapp.metatrader.app.features.chart.market

import com.tradingapp.metatrader.app.core.oanda.OandaSettingsStore
import com.tradingapp.metatrader.app.data.local.cache.CandleCacheRepository
import com.tradingapp.metatrader.app.features.chart.feed.ChartFeedRenderer
import com.tradingapp.metatrader.app.features.chart.webview.ChartWebView
import com.tradingapp.metatrader.app.features.live.LiveCandleFeed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ChartMarketController(
    private val settingsStore: OandaSettingsStore,
    private val cache: CandleCacheRepository,
    private val scope: CoroutineScope,
    private val webView: ChartWebView,
    private val onStatus: (String) -> Unit = {},
    private val renderer: ChartFeedRenderer
) {
    private var job: Job? = null

    fun isConnected(): Boolean = job != null

    fun connect(symbol: String, timeframe: String) {
        if (job != null) return

        val feed = LiveCandleFeed(settingsStore, cache).apply {
            renderCount = 800
            cacheKeep = 5000
        }

        job = scope.launch {
            try {
                feed.stream(symbol, timeframe).collect { upd ->
                    renderer.apply(upd)
                }
            } catch (t: Throwable) {
                onStatus("Controller error: ${t.message}")
            }
        }
    }

    fun disconnect() {
        job?.cancel()
        job = null
        onStatus("Disconnected.")
    }
}
