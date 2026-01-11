package com.tradingapp.metatrader.app.core.expert.supervisor

import com.tradingapp.metatrader.app.core.expert.runtime.ExpertRuntimeMt5
import com.tradingapp.metatrader.app.core.feed.BarCloseDetector
import com.tradingapp.metatrader.app.core.feed.CandleFeed
import com.tradingapp.metatrader.app.core.trading.commands.OrderCommand
import com.tradingapp.metatrader.app.core.trading.commands.OrderCommandBus
import com.tradingapp.metatrader.app.core.trading.mt5sim.InstrumentCatalog
import com.tradingapp.metatrader.app.core.trading.mt5sim.PriceQuote
import com.tradingapp.metatrader.app.core.trading.mt5sim.QuoteBook
import com.tradingapp.metatrader.app.core.trading.mt5sim.VirtualAccountMt5
import com.tradingapp.metatrader.app.core.trading.mt5sim.modifyStops
import com.tradingapp.metatrader.app.features.terminal.tradinghub.TradingHub
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ExpertSessionMt5(
    private val scope: CoroutineScope,
    private val feed: CandleFeed,
    private val symbol: String,
    private val timeframe: String,
    scriptText: String,
    private val hub: TradingHub,
    private val bus: OrderCommandBus,
    private val onEvent: (com.tradingapp.metatrader.app.core.expert.runtime.ExpertRuntimeMt5.Event) -> Unit
) {
    private val account = VirtualAccountMt5(balance = 10_000.0)
    private val runtime = ExpertRuntimeMt5(scriptText, account)

    private val spec = InstrumentCatalog.spec(symbol)
    private val quotes = QuoteBook()

    private var job: Job? = null
    private var cmdJob: Job? = null
    private var lastHistorySize = 0

    fun start() {
        if (job != null) return

        cmdJob?.cancel()
        cmdJob = scope.launch {
            bus.flow.collectLatest { cmd ->
                // pending orders are timeframe-bound (session context), positions are symbol-bound
                when (cmd) {
                    is OrderCommand.PlacePending -> {
                        if (cmd.symbol != symbol || cmd.timeframe != timeframe) return@collectLatest
                        val nowSec = (System.currentTimeMillis() / 1000L)
                        account.placePending(
                            symbol = cmd.symbol,
                            timeframe = cmd.timeframe,
                            type = cmd.type,
                            lots = cmd.lots,
                            entryPrice = cmd.entryPrice,
                            sl = cmd.sl,
                            tp = cmd.tp,
                            comment = cmd.comment,
                            createdTimeSec = nowSec
                        )
                    }
                    is OrderCommand.CancelPending -> {
                        if (cmd.symbol != symbol || cmd.timeframe != timeframe) return@collectLatest
                        account.cancelPending(cmd.orderId)
                    }
                    is OrderCommand.ModifyPositionStops -> {
                        if (cmd.symbol != symbol) return@collectLatest
                        account.modifyStops(cmd.positionId, cmd.newSl, cmd.newTp)
                    }
                    is OrderCommand.ClosePartial -> {
                        if (cmd.symbol != symbol) return@collectLatest
                        val q = quotes.get(symbol) ?: return@collectLatest
                        account.closePartial(spec, cmd.positionId, cmd.closeLots, q, reason = "MANUAL")
                    }
                }

                hub.updateFromSession(
                    positions = account.positions.toList(),
                    orders = account.pendingOrders.toList(),
                    dealsDelta = emptyList()
                )
            }
        }

        job = scope.launch {
            val updates = feed.stream(symbol, timeframe)
            BarCloseDetector.closedBars(updates).collect { closed ->
                val q = PriceQuote(timeSec = closed.timeSec, bid = closed.close, ask = closed.close)
                quotes.set(symbol, q)

                account.checkPendingOnQuote(spec, q)

                val events = runtime.onClosedBar(symbol, timeframe, closed)
                for (e in events) onEvent(e)

                account.checkStopsOnCandle(
                    spec = spec,
                    candleTimeSec = closed.timeSec,
                    candleHigh = closed.high,
                    candleLow = closed.low,
                    candleClose = closed.close,
                    conservativeWorstCase = true
                )

                val dealsDelta = if (account.history.size >= lastHistorySize) {
                    account.history.subList(lastHistorySize, account.history.size).toList()
                } else emptyList()
                lastHistorySize = account.history.size

                hub.updateFromSession(
                    positions = account.positions.toList(),
                    orders = account.pendingOrders.toList(),
                    dealsDelta = dealsDelta
                )
            }
        }
    }

    fun stop() {
        job?.cancel(); job = null
        cmdJob?.cancel(); cmdJob = null
    }
}
