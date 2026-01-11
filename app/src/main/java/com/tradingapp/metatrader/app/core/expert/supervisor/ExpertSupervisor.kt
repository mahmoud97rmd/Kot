package com.tradingapp.metatrader.app.core.expert.supervisor

import com.tradingapp.metatrader.app.core.autotrading.AutoTradingStore
import com.tradingapp.metatrader.app.core.oanda.OandaSettingsStore
import com.tradingapp.metatrader.app.data.local.cache.CandleCacheRepository
import com.tradingapp.metatrader.app.features.expert.data.ExpertAttachmentRepository
import com.tradingapp.metatrader.app.features.expert.data.ExpertScriptRepository
import com.tradingapp.metatrader.app.features.journal.logs.ExpertsBus
import com.tradingapp.metatrader.app.features.journal.logs.JournalBus
import com.tradingapp.metatrader.app.features.live.LiveCandleFeed
import com.tradingapp.metatrader.app.features.terminal.tradinghub.TradingHub
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpertSupervisor @Inject constructor(
    private val autoTradingStore: AutoTradingStore,
    private val attachments: ExpertAttachmentRepository,
    private val scripts: ExpertScriptRepository,
    private val settingsStore: OandaSettingsStore,
    private val cache: CandleCacheRepository,
    private val hub: TradingHub,
    private val journal: JournalBus,
    private val expertsBus: ExpertsBus
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private var job: Job? = null
    private val sessions = linkedMapOf<String, ExpertSessionMt5>()

    private val logSink = BusLogSink(journal, expertsBus, "ExpertSupervisor")
    private val markerSink = NoopMarkerSink // wiring later

    fun startWatching() {
        if (job != null) return

        job = scope.launch {
            autoTradingStore.enabledFlow.collect { enabled ->
                if (!enabled) {
                    logSink.log("AutoTrading OFF -> stopping all EA sessions")
                    stopAll()
                } else {
                    logSink.log("AutoTrading ON -> maintaining EA sessions")
                    maintainLoop()
                }
            }
        }
    }

    private suspend fun maintainLoop() {
        while (autoTradingStore.enabledFlow.value == true) {
            try {
                val desired = buildDesiredExperts()
                reconcile(desired)
            } catch (t: Throwable) {
                logSink.log("Maintain loop error: ${t.message}")
            }
            delay(2000L)
        }
    }

    private suspend fun buildDesiredExperts(): List<AttachedExpert> {
        val atts = attachments.getAll()
        val allScripts = scripts.getAll()
        val map = allScripts.associateBy { it.id }

        val out = ArrayList<AttachedExpert>()
        for (a in atts) {
            val s = map[a.scriptId] ?: continue
            val txt = s.content
            if (txt.isBlank()) continue
            out.add(
                AttachedExpert(
                    scriptId = s.id,
                    scriptName = s.name,
                    scriptText = txt,
                    symbol = a.symbol,
                    timeframe = a.timeframe
                )
            )
        }
        return out
    }

    private fun key(symbol: String, timeframe: String): String = "$symbol|$timeframe"

    private fun reconcile(desired: List<AttachedExpert>) {
        val desiredKeys = desired.map { key(it.symbol, it.timeframe) }.toSet()

        val toStop = sessions.keys.filter { it !in desiredKeys }
        for (k in toStop) {
            sessions.remove(k)?.stop()
            logSink.log("Stopped EA session: $k")
        }

        for (d in desired) {
            val k = key(d.symbol, d.timeframe)
            if (sessions.containsKey(k)) continue

            val feed = LiveCandleFeed(settingsStore, cache).apply {
                renderCount = 800
                cacheKeep = 5000
            }

            val session = ExpertSessionMt5(
                scope = scope,
                feed = feed,
                symbol = d.symbol,
                timeframe = d.timeframe,
                scriptText = d.scriptText,
                hub = hub,
                onEvent = { e ->
                    logSink.logExpert(
                        symbol = d.symbol,
                        timeframe = d.timeframe,
                        expertName = d.scriptName,
                        level = com.tradingapp.metatrader.app.features.journal.logs.LogLevel.INFO,
                        msg = "${e.type} ${e.message}"
                    )
                    when {
                        e.message.startsWith("BUY") -> markerSink.onBuy(e.timeSec, "BUY")
                        e.message.startsWith("SELL") -> markerSink.onSell(e.timeSec, "SELL")
                        e.type == "CLOSE" -> markerSink.onClose(e.timeSec, "CLOSE")
                    }
                }
            )
            sessions[k] = session
            session.start()
            logSink.log("Started EA '${d.scriptName}' on ${d.symbol} ${d.timeframe}")
        }
    }

    private fun stopAll() {
        for ((k, s) in sessions) {
            s.stop()
            logSink.log("Stopped EA session: $k")
        }
        sessions.clear()
    }
}
