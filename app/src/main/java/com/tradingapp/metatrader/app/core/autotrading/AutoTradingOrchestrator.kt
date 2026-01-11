package com.tradingapp.metatrader.app.core.autotrading

import com.tradingapp.metatrader.app.core.expert.supervisor.ExpertSessionMt5
import com.tradingapp.metatrader.app.core.feed.CandleFeed
import com.tradingapp.metatrader.app.core.journal.JournalLogger
import com.tradingapp.metatrader.app.core.trading.commands.OrderCommandBus
import com.tradingapp.metatrader.app.features.expert.data.ExpertAttachmentRepository
import com.tradingapp.metatrader.app.features.expert.data.ExpertScriptRepository
import com.tradingapp.metatrader.app.features.terminal.tradinghub.TradingHub
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutoTradingOrchestrator @Inject constructor(
    private val scope: CoroutineScope,
    private val autoStore: AutoTradingStore,
    private val attachments: ExpertAttachmentRepository,
    private val scripts: ExpertScriptRepository,
    private val feed: CandleFeed,
    private val hub: TradingHub,
    private val logger: JournalLogger,
    private val bus: OrderCommandBus
) {
    private val sessions = HashMap<String, ExpertSessionMt5>()
    private var watchJob: Job? = null

    fun startWatching() {
        if (watchJob != null) return
        watchJob = scope.launch(Dispatchers.Default) {
            autoStore.enabledFlow.collectLatest { enabled ->
                if (!enabled) {
                    stopAll()
                    return@collectLatest
                }
                attachments.flow.collectLatest { attachList ->
                    // attachList: list of (scriptId,symbol,timeframe)
                    val desiredKeys = attachList.map { "${it.symbol}|${it.timeframe}" }.toSet()

                    // stop removed
                    val toStop = sessions.keys.filter { it !in desiredKeys }
                    for (k in toStop) {
                        sessions.remove(k)?.stop()
                    }

                    // start new
                    for (a in attachList) {
                        val key = "${a.symbol}|${a.timeframe}"
                        if (sessions.containsKey(key)) continue
                        val script = scripts.getById(a.scriptId) ?: continue

                        val session = ExpertSessionMt5(
                            scope = scope,
                            feed = feed,
                            symbol = a.symbol,
                            timeframe = a.timeframe,
                            scriptText = script.scriptText,
                            hub = hub,
                            bus = bus,
                            onEvent = { ev -> logger.log("[EA ${script.name}] ${ev.message}") }
                        )
                        sessions[key] = session
                        session.start()
                    }
                }
            }
        }
    }

    private fun stopAll() {
        for (s in sessions.values) s.stop()
        sessions.clear()
        hub.clear()
    }
}
