package com.tradingapp.metatrader.app.features.tester.engine

import com.tradingapp.metatrader.app.core.candles.CandleMappers
import com.tradingapp.metatrader.app.core.oanda.OandaSettingsStore
import com.tradingapp.metatrader.app.data.local.cache.CandleCacheRepository
import com.tradingapp.metatrader.app.data.remote.oanda.http.OandaEndpoints
import com.tradingapp.metatrader.app.data.remote.oanda.http.OandaHttpClient
import com.tradingapp.metatrader.app.data.remote.oanda.rest.OandaRestClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OandaHistoryDownloader @Inject constructor(
    private val settingsStore: OandaSettingsStore,
    private val cache: CandleCacheRepository
) {
    suspend fun downloadIntoCache(
        symbol: String,
        timeframe: String,
        count: Int,
        onStatus: (String) -> Unit = {}
    ): Int = withContext(Dispatchers.Default) {
        onStatus("Loading OANDA settings...")
        val settings = settingsStore.getNow()
            ?: throw IllegalStateException("OANDA not configured. Set token/account in OANDA settings.")

        val restBase = OandaEndpoints.restBaseUrl(settings.environment)
        val http = OandaHttpClient.create { settings.apiToken }
        val rest = OandaRestClient(http)

        val safeCount = count.coerceIn(50, 5000)
        onStatus("Downloading $safeCount candles from OANDA ($symbol $timeframe)...")

        val candles = withContext(Dispatchers.IO) {
            rest.getCandles(
                envBaseUrl = restBase,
                instrument = symbol,
                granularity = timeframe,
                count = safeCount
            )
        }.map { CandleMappers.fromMarket(it) }

        onStatus("Saving to cache...")
        withContext(Dispatchers.IO) {
            cache.upsertUnified(symbol, timeframe, candles)
            cache.trimKeepLast(symbol, timeframe, keepCount = 20_000)
        }

        onStatus("Download done. Saved=${candles.size}")
        candles.size
    }
}
