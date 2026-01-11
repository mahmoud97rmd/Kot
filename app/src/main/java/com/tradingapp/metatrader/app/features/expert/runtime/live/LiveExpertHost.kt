package com.tradingapp.metatrader.app.features.expert.runtime.live

import com.tradingapp.metatrader.app.core.autotrading.AutoTradingStore
import com.tradingapp.metatrader.app.core.market.MarketTick
import com.tradingapp.metatrader.app.core.market.feed.MarketFeed
import com.tradingapp.metatrader.app.core.time.TimeframeParser
import com.tradingapp.metatrader.app.core.trading.OrderSide
import com.tradingapp.metatrader.app.core.trading.TradeExecutor
import com.tradingapp.metatrader.app.core.trading.positions.PositionService
import com.tradingapp.metatrader.app.features.chart.markers.ChartMarker
import com.tradingapp.metatrader.app.features.chart.markers.live.LiveMarkerBus
import com.tradingapp.metatrader.app.features.expert.engine.runtime.ExpertRuntime
import com.tradingapp.metatrader.app.features.expert.engine.runtime.RhinoExpertRuntime
import com.tradingapp.metatrader.app.features.expert.engine.shared.BarSnapshot
import com.tradingapp.metatrader.app.features.expert.engine.shared.ExpertAction
import com.tradingapp.metatrader.app.features.expert.engine.shared.ExpertSafetyGate
import com.tradingapp.metatrader.app.features.expert.engine.shared.TickSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class LiveExpertHost(
    private val symbol: String,
    private val timeframe: String,
    private val expertName: String,
    private val expertCode: String,
    private val feed: MarketFeed,
    private val executor: TradeExecutor,
    private val positions: PositionService,
    private val markerBus: LiveMarkerBus,
    private val autoTrading: AutoTradingStore,
    private val log: (String) -> Unit
) {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var job: Job? = null
    private val running = AtomicBoolean(false)

    private val runtime: ExpertRuntime = RhinoExpertRuntime()
    private val safety = ExpertSafetyGate(cooldownMs = 2_000, maxPositions = 1)

    private var lastBarOpenSec: Long = -1
    private var barOpen: Double = 0.0
    private var barHigh: Double = 0.0
    private var barLow: Double = 0.0
    private var barClose: Double = 0.0

    fun start() {
        if (running.getAndSet(true)) return

        runtime.init(expertCode = expertCode, expertName = expertName, symbol = symbol, timeframe = timeframe)
        runtime.onInit().forEach { handleAction(it) }

        log("[$expertName] START on $symbol $timeframe")

        val barSizeSec = TimeframeParser.toSeconds(timeframe)

        job = scope.launch {
            feed.ticks(listOf(symbol)).collectLatest { tick ->
                onTick(tick, barSizeSec)
            }
        }
    }

    fun stop() {
        if (!running.getAndSet(false)) return
        job?.cancel()
        job = null
        runtime.close()
        log("[$expertName] STOP on $symbol $timeframe")
    }

    private fun onTick(t: MarketTick, barSizeSec: Long) {
        val nowSec = t.timeEpochMs / 1000L
        val barOpenSec = (nowSec / barSizeSec) * barSizeSec

        val mid = (t.bid + t.ask) / 2.0

        if (barOpenSec != lastBarOpenSec) {
            if (lastBarOpenSec >= 0) {
                safety.onNewBar(barOpenSec)

                val bar = BarSnapshot(
                    symbol = symbol,
                    timeframe = timeframe,
                    openTimeSec = lastBarOpenSec,
                    open = barOpen,
                    high = barHigh,
                    low = barLow,
                    close = barClose
                )
                runtime.onBar(bar).forEach { handleAction(it) }
            }

            lastBarOpenSec = barOpenSec
            barOpen = mid
            barHigh = mid
            barLow = mid
            barClose = mid

            log("[$expertName] NEW BAR $symbol $timeframe t=$barOpenSec")
        } else {
            barHigh = maxOf(barHigh, mid)
            barLow = minOf(barLow, mid)
            barClose = mid
        }

        val tick = TickSnapshot(symbol = symbol, timeEpochMs = t.timeEpochMs, bid = t.bid, ask = t.ask)
        runtime.onTick(tick).forEach { handleAction(it) }
    }

    private fun handleAction(a: ExpertAction) {
        when (a) {
            is ExpertAction.Log -> log("[$expertName] ${a.level}: ${a.message}")
            is ExpertAction.MarketBuy -> scope.launch { placeOrder(OrderSide.BUY, a.units, a.tp, a.sl) }
            is ExpertAction.MarketSell -> scope.launch { placeOrder(OrderSide.SELL, a.units, a.tp, a.sl) }
            ExpertAction.CloseAll -> scope.launch { closeAllForInstrument() }
        }
    }

    private suspend fun ensureAutoTradingEnabled(): Boolean {
        val enabled = autoTrading.isEnabledNow()
        if (!enabled) {
            log("[$expertName] BLOCK action: AutoTrading is OFF")
        }
        return enabled
    }

    private suspend fun syncOpenPositionsForSymbol() {
        val list = positions.getOpenPositions()
        val p = list.firstOrNull { it.instrument == symbol }
        val count = if (p != null && p.hasAny) 1 else 0
        safety.setOpenPositions(count)
    }

    private suspend fun placeOrder(side: OrderSide, units: Long, tp: Double?, sl: Double?) {
        if (!running.get()) return
        if (!ensureAutoTradingEnabled()) return

        syncOpenPositionsForSymbol()

        val now = System.currentTimeMillis()
        if (!safety.canPlaceOrder(now)) {
            log("[$expertName] BLOCK order (safety): $side units=$units")
            return
        }

        val req = com.tradingapp.metatrader.app.core.trading.MarketOrderRequest(
            symbol = symbol,
            side = side,
            units = units,
            takeProfitPrice = tp,
            stopLossPrice = sl
        )

        val res = executor.placeMarketOrder(req)
        if (!res.ok) {
            log("[$expertName] ORDER FAIL: ${res.message}")
            res.raw?.let { log(it.take(600)) }
            return
        }

        safety.markOrderPlaced(now)

        markerBus.post(
            ChartMarker(
                timeSec = if (lastBarOpenSec > 0) lastBarOpenSec else (System.currentTimeMillis() / 1000L),
                position = if (side == OrderSide.BUY) "belowBar" else "aboveBar",
                color = if (side == OrderSide.BUY) "#26a69a" else "#ef5350",
                shape = if (side == OrderSide.BUY) "arrowUp" else "arrowDown",
                text = "${expertName}: ${side.name} $units"
            )
        )

        log("[$expertName] ORDER OK: ${side.name} units=$units tp=$tp sl=$sl")
    }

    private suspend fun closeAllForInstrument() {
        if (!running.get()) return
        if (!ensureAutoTradingEnabled()) return

        val res = positions.closeInstrumentAll(symbol)
        if (!res.ok) {
            log("[$expertName] CLOSE FAIL: ${res.message}")
            res.raw?.let { log(it.take(600)) }
            return
        }

        markerBus.post(
            ChartMarker(
                timeSec = if (lastBarOpenSec > 0) lastBarOpenSec else (System.currentTimeMillis() / 1000L),
                position = "aboveBar",
                color = "#4caf50",
                shape = "circle",
                text = "${expertName}: CloseAll"
            )
        )

        log("[$expertName] CLOSE OK for $symbol")
        syncOpenPositionsForSymbol()
    }
}
