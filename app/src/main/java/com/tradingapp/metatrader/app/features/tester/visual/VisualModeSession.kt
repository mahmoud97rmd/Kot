package com.tradingapp.metatrader.app.features.tester.visual

import com.tradingapp.metatrader.app.core.expert.dsl.ExpertDslParser
import com.tradingapp.metatrader.app.core.expert.runtime.ExpertRuntime
import com.tradingapp.metatrader.app.core.feed.CandleUpdate
import com.tradingapp.metatrader.app.core.trading.sim.VirtualAccount
import com.tradingapp.metatrader.app.features.chart.feed.ChartFeedRenderer
import com.tradingapp.metatrader.app.features.replay.ReplayCandleFeed

class VisualModeSession(
    private val replayFeed: ReplayCandleFeed,
    private val renderer: ChartFeedRenderer,
    private val addMarkerJson: (String) -> Unit,
    private val onStatus: (String) -> Unit
) {
    suspend fun run(symbol: String, timeframe: String, scriptText: String) {
        // Parse EA
        val model = ExpertDslParser().parse(scriptText)

        val account = VirtualAccount(balance = 10_000.0)
        val runtime = ExpertRuntime(model, account)

        replayFeed.stream(symbol, timeframe).collect { upd ->
            // draw chart
            renderer.apply(upd)

            // EA processing
            if (upd is CandleUpdate.Current) {
                val events = runtime.onCandle(symbol, timeframe, upd.candle)
                for (e in events) {
                    when {
                        e.message.startsWith("BUY ") -> addMarkerJson(VisualMarkerJson.buy(e.timeSec, "BUY"))
                        e.message.startsWith("SELL ") -> addMarkerJson(VisualMarkerJson.sell(e.timeSec, "SELL"))
                        e.type == "CLOSE" -> addMarkerJson(VisualMarkerJson.close(e.timeSec, "CLOSE"))
                    }
                    onStatus("EA: ${e.message} | Bal=${account.balance}")
                }
            }
        }
    }
}
