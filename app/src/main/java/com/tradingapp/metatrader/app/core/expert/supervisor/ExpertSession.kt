package com.tradingapp.metatrader.app.core.expert.supervisor

import com.tradingapp.metatrader.app.core.expert.dsl.ExpertDslParser
import com.tradingapp.metatrader.app.core.expert.runtime.ExpertRuntime
import com.tradingapp.metatrader.app.core.feed.BarCloseDetector
import com.tradingapp.metatrader.app.core.feed.CandleFeed
import com.tradingapp.metatrader.app.core.trading.sim.VirtualAccount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ExpertSession(
    private val scope: CoroutineScope,
    private val feed: CandleFeed,
    private val symbol: String,
    private val timeframe: String,
    scriptText: String,
    private val onEvent: (ExpertRuntime.Event) -> Unit
) {
    private val account = VirtualAccount(balance = 10_000.0)
    private val runtime: ExpertRuntime = ExpertRuntime(ExpertDslParser().parse(scriptText), account)

    private var job: Job? = null

    fun start() {
        if (job != null) return
        job = scope.launch {
            val updates = feed.stream(symbol, timeframe)
            BarCloseDetector.closedBars(updates).collect { closedBar ->
                val events = runtime.onCandle(symbol, timeframe, closedBar)
                for (e in events) onEvent(e)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}
