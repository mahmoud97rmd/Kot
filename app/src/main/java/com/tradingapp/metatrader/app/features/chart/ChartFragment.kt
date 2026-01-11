package com.tradingapp.metatrader.app.features.chart

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.tradingapp.metatrader.app.databinding.FragmentChartBinding
import com.tradingapp.metatrader.app.features.drawing.DrawingViewModel
import com.tradingapp.metatrader.app.features.drawingui.DrawingStyleBottomSheet
import com.tradingapp.metatrader.app.features.replay.ReplayViewModel
import com.tradingapp.metatrader.app.features.strategy.AutoTradingViewModel
import com.tradingapp.metatrader.app.features.ticket.TradeTicketBottomSheet
import com.tradingapp.metatrader.app.state.AppStateViewModel
import com.tradingapp.metatrader.app.utils.MarketSessionUtil
import com.tradingapp.metatrader.domain.models.Timeframe
import com.tradingapp.metatrader.domain.models.trading.Position
import com.tradingapp.metatrader.domain.models.trading.TradingEvent
import com.tradingapp.metatrader.domain.usecases.trading.ExecuteMarketOrderUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ChartFragment : Fragment(), DrawingStyleBottomSheet.Listener {

    private var _binding: FragmentChartBinding? = null
    private val binding get() = _binding!!

    private val vm: ChartViewModel by viewModels()
    private val eventsVm: ChartEventsViewModel by viewModels()
    private val riskVm: ChartRiskViewModel by viewModels()
    private val replayVm: ReplayViewModel by viewModels()
    private val autoVm: AutoTradingViewModel by viewModels()
    private val drawingVm: DrawingViewModel by viewModels()
    private val appState: AppStateViewModel by activityViewModels()

    @Inject lateinit var executeMarket: ExecuteMarketOrderUseCase

    private var webReady = false
    private var lastHistorySentKey: String? = null
    private var lastDrawingsSentKey: String? = null

    private var primaryPositionId: String? = null
    private var primaryInstrument: String? = null
    private var primarySL: Double? = null
    private var primaryTP: Double? = null

    private var selectedDrawingJson: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupWebView(binding.chartWebView)

        // Drawing toolbar
        binding.drawOffBtn.setOnClickListener { setTool("NONE") }
        binding.drawHLineBtn.setOnClickListener { setTool("HLINE") }
        binding.drawTrendBtn.setOnClickListener { setTool("TREND") }
        binding.drawClearBtn.setOnClickListener { drawingVm.clear() }

        binding.drawDeleteBtn.setOnClickListener {
            if (webReady) binding.chartWebView.evaluateJavascript("deleteSelectedDrawing();", null)
        }
        binding.drawLockBtn.setOnClickListener {
            if (webReady) binding.chartWebView.evaluateJavascript("toggleLockSelectedDrawing();", null)
        }
        binding.drawEditBtn.setOnClickListener {
            val js = selectedDrawingJson ?: return@setOnClickListener
            val obj = runCatching { JSONObject(js) }.getOrNull() ?: return@setOnClickListener
            val id = obj.optString("id", "")
            val type = obj.optString("type", "")
            val color = obj.optString("colorHex", "#FFFFFF")
            val width = obj.optDouble("lineWidth", 2.0).toFloat()
            val locked = obj.optBoolean("locked", false)
            if (id.isBlank()) return@setOnClickListener

            DrawingStyleBottomSheet.newInstance(id, type, color, width, locked)
                .show(parentFragmentManager, "drawing_style")
        }

        // Replay UI
        val speeds = listOf("x1", "x2", "x5", "x10")
        binding.replaySpeedSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, speeds)
        binding.replaySpeedSpinner.setSelection(0)

        // Session status
        fun refreshSession() {
            val open = MarketSessionUtil.isMarketOpen()
            binding.sessionText.text = MarketSessionUtil.statusText()
            val enabled = open && !replayVm.state.value.enabled
            binding.buyBtn.isEnabled = enabled
            binding.sellBtn.isEnabled = enabled
        }
        refreshSession()

        // AUTO toggle
        binding.autoBtn.setOnClickListener {
            val enabled = !appState.autoTradingEnabled.value
            appState.setAutoTradingEnabled(enabled)
            if (!enabled) autoVm.reset()
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            appState.autoTradingEnabled.collectLatest { enabled ->
                binding.autoBtn.text = if (enabled) "AUTO: ON" else "AUTO: OFF"
            }
        }

        // One-click toggle
        binding.oneClickBtn.setOnClickListener {
            val enabled = !appState.oneClickEnabled.value
            appState.setOneClickEnabled(enabled)
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            appState.oneClickEnabled.collectLatest { enabled ->
                binding.oneClickBtn.text = if (enabled) "One-Click: ON" else "One-Click: OFF"
            }
        }

        // Lots stepper
        binding.lotsMinusBtn.setOnClickListener { appState.decLots(0.01) }
        binding.lotsPlusBtn.setOnClickListener { appState.incLots(0.01) }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            appState.quickLots.collectLatest { lots ->
                binding.lotsText.text = String.format(Locale.US, "Lots: %.2f", lots)
            }
        }

        // Buy/Sell behavior
        binding.buyBtn.setOnClickListener { handleOneClick(side = Position.Side.BUY) }
        binding.sellBtn.setOnClickListener { handleOneClick(side = Position.Side.SELL) }

        // Ticket shortcuts
        binding.priceText.setOnLongClickListener {
            TradeTicketBottomSheet.newTrade(vm.state.value.instrument).show(parentFragmentManager, "ticket_new")
            true
        }
        binding.symbolText.setOnLongClickListener {
            TradeTicketBottomSheet.newTrade(vm.state.value.instrument).show(parentFragmentManager, "ticket_new")
            true
        }

        // Replay controls
        binding.replayToggleBtn.setOnClickListener {
            val enabled = !replayVm.state.value.enabled
            val inst = vm.state.value.instrument
            appState.setReplayMode(enabled)
            replayVm.setEnabled(enabled, inst)
            autoVm.reset()
            refreshSession()
        }

        binding.replayPlayPauseBtn.setOnClickListener {
            val st = replayVm.state.value
            if (!st.enabled) return@setOnClickListener
            if (st.playing) replayVm.pause() else replayVm.play()
        }

        binding.replaySpeedSpinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) {
                val sp = when (pos) { 0 -> 1; 1 -> 2; 2 -> 5; else -> 10 }
                replayVm.setSpeed(sp)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
        })

        // Instrument changes
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            appState.selectedInstrument.collectLatest { inst ->
                appState.setReplayMode(false)
                replayVm.setEnabled(false, inst)
                vm.startOrRestart(inst, Timeframe.M1)
                lastHistorySentKey = null
                lastDrawingsSentKey = null
                autoVm.reset()
                refreshSession()

                drawingVm.start(inst, Timeframe.M1)

                selectedDrawingJson = null
                if (webReady) binding.chartWebView.evaluateJavascript("clearSelection();", null)
            }
        }

        // Drawings observer -> JS
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            drawingVm.current.collectLatest { list ->
                if (!webReady) return@collectLatest
                val inst = if (replayVm.state.value.enabled) replayVm.state.value.instrument else vm.state.value.instrument
                val key = "${inst}_M1"
                val json = drawingVm.toJson(list)
                binding.chartWebView.evaluateJavascript("setDrawings(${JSONObject.quote(json)});", null)
                lastDrawingsSentKey = key
            }
        }

        // Price panel (bid/ask/spread) computed from mid as fallback
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            appState.prices.collectLatest { map ->
                val inst = if (replayVm.state.value.enabled) replayVm.state.value.instrument else vm.state.value.instrument
                val mid = map[inst]
                if (mid == null) {
                    binding.bidText.text = "Bid: --"
                    binding.askText.text = "Ask: --"
                    binding.spreadText.text = "Spr: --"
                } else {
                    val spr = syntheticSpread(inst, mid)
                    val bid = mid - spr / 2.0
                    val ask = mid + spr / 2.0
                    binding.bidText.text = String.format(Locale.US, "Bid: %.3f", bid)
                    binding.askText.text = String.format(Locale.US, "Ask: %.3f", ask)
                    binding.spreadText.text = String.format(Locale.US, "Spr: %.3f", spr)
                }
            }
        }

        // Replay rendering
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            replayVm.state.collectLatest { st ->
                binding.replayToggleBtn.text = if (st.enabled) "Replay: ON" else "Replay: OFF"
                binding.replayPlayPauseBtn.text = if (st.playing) "Pause" else "Play"
                binding.replayStatus.text = if (!st.enabled) "--" else "${st.index + 1}/${st.total}"

                refreshSession()

                if (!st.enabled || !webReady) return@collectLatest

                drawingVm.start(st.instrument, Timeframe.M1)

                binding.symbolText.text = st.instrument
                val mid = st.current?.close
                binding.priceText.text = mid?.let { String.format(Locale.US, "%.3f", it) } ?: "--"
                if (mid != null) appState.updatePrice(st.instrument, mid)

                val arr = JSONArray()
                for (c in st.historyWindow) {
                    arr.put(JSONObject().apply {
                        put("time", c.time.epochSecond)
                        put("open", c.open)
                        put("high", c.high)
                        put("low", c.low)
                        put("close", c.close)
                    })
                }
                binding.chartWebView.evaluateJavascript("setHistory(${arr.toString()});", null)

                st.current?.let { c ->
                    val obj = JSONObject().apply {
                        put("time", c.time.epochSecond)
                        put("open", c.open)
                        put("high", c.high)
                        put("low", c.low)
                        put("close", c.close)
                    }
                    binding.chartWebView.evaluateJavascript("updateLastCandle(${obj.toString()});", null)
                }

                binding.chartWebView.evaluateJavascript("setPrimaryRiskLines(null, null);", null)

                if (appState.autoTradingEnabled.value && st.historyWindow.isNotEmpty()) {
                    autoVm.onNewCandleClosed(
                        instrument = st.instrument,
                        closedHistory = st.historyWindow,
                        lastPriceForMarket = mid ?: st.historyWindow.last().close
                    )
                }
            }
        }

        // Live rendering + AUTO
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.state.collectLatest { st ->
                if (replayVm.state.value.enabled) return@collectLatest

                drawingVm.start(st.instrument, Timeframe.M1)

                binding.symbolText.text = st.instrument
                binding.priceText.text = st.lastPrice?.let { String.format(Locale.US, "%.3f", it) } ?: "--"
                if (!webReady) return@collectLatest

                st.lastPrice?.let { appState.updatePrice(st.instrument, it) }

                val key = "${st.instrument}_${st.timeframe.name}"
                if (st.history.isNotEmpty() && lastHistorySentKey != key) {
                    val arr = JSONArray()
                    for (c in st.history) {
                        arr.put(JSONObject().apply {
                            put("time", c.time.epochSecond)
                            put("open", c.open)
                            put("high", c.high)
                            put("low", c.low)
                            put("close", c.close)
                        })
                    }
                    binding.chartWebView.evaluateJavascript("setHistory(${arr.toString()});", null)
                    lastHistorySentKey = key
                }

                st.lastCandle?.let { c ->
                    val obj = JSONObject().apply {
                        put("time", c.time.epochSecond)
                        put("open", c.open)
                        put("high", c.high)
                        put("low", c.low)
                        put("close", c.close)
                    }
                    binding.chartWebView.evaluateJavascript("updateLastCandle(${obj.toString()});", null)
                }

                val price = st.lastPrice
                if (appState.autoTradingEnabled.value && price != null && st.history.isNotEmpty()) {
                    autoVm.onNewCandleClosed(
                        instrument = st.instrument,
                        closedHistory = st.history,
                        lastPriceForMarket = price
                    )
                }
            }
        }

        // Risk lines (live)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            riskVm.positions.collectLatest { positions ->
                if (!webReady) return@collectLatest
                if (replayVm.state.value.enabled) {
                    binding.chartWebView.evaluateJavascript("setRiskLinesMulti([]);", null)
                    binding.chartWebView.evaluateJavascript("setPrimaryRiskLines(null, null);", null)
                    return@collectLatest
                }

                val inst = vm.state.value.instrument
                val same = positions.filter { it.instrument == inst }

                val lines = JSONArray()
                var slCount = 0
                var tpCount = 0
                for (p in same) {
                    p.stopLoss?.let {
                        slCount += 1
                        lines.put(JSONObject().apply { put("price", it); put("kind", "SL"); put("label", "SL$slCount") })
                    }
                    p.takeProfit?.let {
                        tpCount += 1
                        lines.put(JSONObject().apply { put("price", it); put("kind", "TP"); put("label", "TP$tpCount") })
                    }
                }
                binding.chartWebView.evaluateJavascript("setRiskLinesMulti(${lines.toString()});", null)

                val primary = same.firstOrNull()
                primaryPositionId = primary?.id
                primaryInstrument = primary?.instrument
                primarySL = primary?.stopLoss
                primaryTP = primary?.takeProfit

                val js = "setPrimaryRiskLines(${primarySL ?: "null"}, ${primaryTP ?: "null"});"
                binding.chartWebView.evaluateJavascript(js, null)
            }
        }

        // Events -> markers
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            eventsVm.events.collectLatest { ev ->
                if (!webReady || ev == null) return@collectLatest
                val activeInstrument =
                    if (replayVm.state.value.enabled) replayVm.state.value.instrument
                    else vm.state.value.instrument

                when (ev) {
                    is TradingEvent.PositionOpened -> {
                        if (ev.position.instrument != activeInstrument) return@collectLatest
                        val t = ev.position.entryTime.epochSecond
                        val side = ev.position.side.name
                        binding.chartWebView.evaluateJavascript("addTradeMarker($t, '$side', '$side');", null)
                    }
                    is TradingEvent.PositionClosed -> {
                        if (ev.trade.instrument != activeInstrument) return@collectLatest
                        val t = ev.trade.exitTime.epochSecond
                        binding.chartWebView.evaluateJavascript("addTradeMarker($t, 'CLOSE', 'CLOSE');", null)
                    }
                    is TradingEvent.PendingPlaced -> {
                        if (ev.order.instrument != activeInstrument) return@collectLatest
                        val t = ev.order.createdAt.epochSecond
                        binding.chartWebView.evaluateJavascript("addTradeMarker($t, 'CLOSE', 'P');", null)
                    }
                    is TradingEvent.PendingTriggered -> {
                        val t = ev.triggerTime.epochSecond
                        binding.chartWebView.evaluateJavascript("addTradeMarker($t, 'BUY', 'TRG');", null)
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun setTool(t: String) {
        if (!webReady) return
        binding.chartWebView.evaluateJavascript("setTool('$t');", null)
    }

    private fun syntheticSpread(instrument: String, mid: Double): Double {
        return when {
            instrument.contains("XAU", ignoreCase = true) -> 0.4
            instrument.contains("BTC", ignoreCase = true) -> mid * 0.0005
            else -> 0.0002
        }
    }

    private fun handleOneClick(side: Position.Side) {
        if (!MarketSessionUtil.isMarketOpen()) return
        if (replayVm.state.value.enabled) return

        val inst = vm.state.value.instrument
        val mid = appState.prices.value[inst] ?: return

        val spr = syntheticSpread(inst, mid)
        val bid = mid - spr / 2.0
        val ask = mid + spr / 2.0
        val lots = appState.quickLots.value

        if (!appState.oneClickEnabled.value) {
            TradeTicketBottomSheet.newTrade(inst).show(parentFragmentManager, "ticket_new")
            return
        }

        val price = if (side == Position.Side.BUY) ask else bid
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            executeMarket(
                instrument = inst,
                side = side,
                price = price,
                lots = lots,
                stopLoss = null,
                takeProfit = null
            )
        }
    }

    // DrawingStyleBottomSheet.Listener
    override fun onApplyStyle(colorHex: String?, width: Float?) {
        if (!webReady) return
        val c = colorHex?.replace(" ", "")?.takeIf { it.startsWith("#") && (it.length == 7 || it.length == 9) }
        val w = width?.coerceIn(1f, 10f)
        val js = "updateSelectedStyle(${c?.let { JSONObject.quote(it) } ?: "null"}, ${w?.toDouble() ?: "null"});"
        binding.chartWebView.evaluateJavascript(js, null)
    }

    override fun onToggleLock() {
        if (!webReady) return
        binding.chartWebView.evaluateJavascript("toggleLockSelectedDrawing();", null)
    }

    override fun onDelete() {
        if (!webReady) return
        binding.chartWebView.evaluateJavascript("deleteSelectedDrawing();", null)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(wv: WebView) {
        wv.settings.javaScriptEnabled = true
        wv.settings.domStorageEnabled = true
        wv.webChromeClient = WebChromeClient()

        wv.addJavascriptInterface(
            ChartJsBridge(
                onRiskDragged = { kind, price ->
                    if (replayVm.state.value.enabled) return@ChartJsBridge
                    val posId = primaryPositionId ?: return@ChartJsBridge
                    val inst = primaryInstrument ?: vm.state.value.instrument

                    val newSl = if (kind == "SL") price else primarySL
                    val newTp = if (kind == "TP") price else primaryTP

                    TradeTicketBottomSheet
                        .modifyPosition(inst, posId, newSl, newTp)
                        .show(parentFragmentManager, "ticket_modify_position_drag")
                },
                onDrawingsChanged = { json ->
                    drawingVm.saveFromJson(json)
                },
                onDrawingSelected = { jsonOrEmpty ->
                    selectedDrawingJson = jsonOrEmpty.takeIf { it.isNotBlank() }
                }
            ),
            "Android"
        )

        wv.loadUrl("file:///android_asset/chart/index.html")
        webReady = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
