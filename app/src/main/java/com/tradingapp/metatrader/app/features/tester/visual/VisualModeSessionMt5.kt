package com.tradingapp.metatrader.app.features.tester.visual

import com.tradingapp.metatrader.app.core.expert.runtime.ExpertRuntimeMt5
import com.tradingapp.metatrader.app.core.trading.mt5sim.InstrumentCatalog
import com.tradingapp.metatrader.app.core.trading.mt5sim.PriceQuote
import com.tradingapp.metatrader.app.core.trading.mt5sim.VirtualAccountMt5
import com.tradingapp.metatrader.app.features.chart.feed.ChartFeedRenderer
import com.tradingapp.metatrader.app.features.chart.markers.ChartMarkerJson
import com.tradingapp.metatrader.app.features.replay.ReplayCandleFeed
import kotlinx.coroutines.flow.collect

class VisualModeSessionMt5(
    private val replayFeed: ReplayCandleFeed,
    private val renderer: ChartFeedRenderer,
    private val addMarkerJson: (String) -> Unit,
    private val onStatus: (String) -> Unit
) {
    suspend fun run(symbol: String, timeframe: String, scriptText: String) {
        onStatus("Visual: loading...")
        val spec = InstrumentCatalog.spec(symbol)
        val account = VirtualAccountMt5(balance = 10_000.0)
        val runtime = ExpertRuntimeMt5(scriptText, account)

        onStatus("Visual: streaming replay...")
        replayFeed.stream(symbol, timeframe).collect { upd ->
            // render chart update
            renderer.apply(upd)

            // only handle closed bars to emulate MT5 tester
            val closed = upd.closed ?: return@collect

            // pending execution on quote (close)
            val q = PriceQuote(timeSec = closed.timeSec, bid = closed.close, ask = closed.close)
            account.checkPendingOnQuote(spec, q)

            // EA runtime
            val events = runtime.onClosedBar(symbol, timeframe, closed)
            for (e in events) {
                // Add a marker if event contains chart marker json (optional)
                val mj = e.markerJson
                if (mj != null) addMarkerJson(mj)
            }

            // SL/TP/Trailing
            val deals = account.checkStopsOnCandle(
                spec = spec,
                candleTimeSec = closed.timeSec,
                candleHigh = closed.high,
                candleLow = closed.low,
                candleClose = closed.close,
                conservativeWorstCase = true
            )

            // show deals as markers too (optional)
            for (d in deals) {
                val marker = ChartMarkerJson.tradeMarker(
                    timeSec = d.closeTimeSec,
                    text = "${d.reason} ${"%.2f".format(d.profit)}"
                )
                addMarkerJson(marker)
            }
        }
        onStatus("Visual: finished.")
    }
}
