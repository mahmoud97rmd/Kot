package com.tradingapp.metatrader.app.features.expert.runtime.manager

import com.tradingapp.metatrader.app.core.autotrading.AutoTradingStore
import com.tradingapp.metatrader.app.core.journal.LiveJournalBus
import com.tradingapp.metatrader.app.core.market.feed.MarketFeed
import com.tradingapp.metatrader.app.core.trading.TradeExecutor
import com.tradingapp.metatrader.app.core.trading.positions.PositionService
import com.tradingapp.metatrader.app.features.chart.markers.live.LiveMarkerBus
import com.tradingapp.metatrader.app.features.expert.data.ExpertAttachmentRepository
import com.tradingapp.metatrader.app.features.expert.data.ExpertScriptRepository
import com.tradingapp.metatrader.app.features.expert.engine.shared.ExpertCodeComposer
import com.tradingapp.metatrader.app.features.expert.inputs.ExpertInputsStore
import com.tradingapp.metatrader.app.features.expert.runtime.live.LiveExpertHost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpertHostManager @Inject constructor(
    private val attachments: ExpertAttachmentRepository,
    private val scripts: ExpertScriptRepository,
    private val inputs: ExpertInputsStore,
    private val feed: MarketFeed,
    private val executor: TradeExecutor,
    private val positions: PositionService,
    private val markerBus: LiveMarkerBus,
    private val autoTrading: AutoTradingStore,
    private val journal: LiveJournalBus
) {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var job: Job? = null
    private val hosts = LinkedHashMap<String, LiveExpertHost>()

    fun start() {
        if (job != null) return
        journal.post("SYSTEM", "INFO", "ExpertHostManager: START watching attachments")

        job = scope.launch {
            attachments.observeAll().collectLatest { list ->
                val active = list.filter { it.isActive }
                val activeKeys = active.map { "${it.symbol}|${it.timeframe}" }.toSet()

                val toStop = hosts.keys.filter { it !in activeKeys }
                for (k in toStop) {
                    hosts.remove(k)?.stop()
                    journal.post("SYSTEM", "INFO", "Stopped host $k")
                }

                for (a in active) {
                    val key = "${a.symbol}|${a.timeframe}"
                    if (hosts.containsKey(key)) continue

                    val s = scripts.getById(a.scriptId)
                    val name = s?.name ?: "MissingScript"
                    val codeRaw = s?.code ?: ""

                    val inputsJson = runCatching { inputs.getInputsJson(a.scriptId) }.getOrElse { "{}" }
                    val code = ExpertCodeComposer.compose(codeRaw, inputsJson)

                    val host = LiveExpertHost(
                        symbol = a.symbol,
                        timeframe = a.timeframe,
                        expertName = name,
                        expertCode = code,
                        feed = feed,
                        executor = executor,
                        positions = positions,
                        markerBus = markerBus,
                        autoTrading = autoTrading,
                        log = { msg -> journal.post("EA", "INFO", msg) }
                    )
                    hosts[key] = host
                    host.start()
                    journal.post("SYSTEM", "INFO", "Started host $key -> $name")
                }
            }
        }
    }

    fun stop() {
        journal.post("SYSTEM", "WARN", "ExpertHostManager: STOP all")
        job?.cancel()
        job = null
        hosts.values.forEach { it.stop() }
        hosts.clear()
    }
}
