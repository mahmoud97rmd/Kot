package com.tradingapp.metatrader.app.features.chart

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tradingapp.metatrader.app.R
import com.tradingapp.metatrader.app.core.autotrading.AutoTradingOrchestrator
import com.tradingapp.metatrader.app.core.autotrading.AutoTradingStore
import com.tradingapp.metatrader.app.core.journal.ui.LiveJournalActivity
import com.tradingapp.metatrader.app.core.oanda.OandaSettingsStore
import com.tradingapp.metatrader.app.core.trading.commands.OrderCommand
import com.tradingapp.metatrader.app.core.trading.commands.OrderCommandBus
import com.tradingapp.metatrader.app.core.trading.mt5sim.PendingType
import com.tradingapp.metatrader.app.data.local.cache.CandleCacheRepository
import com.tradingapp.metatrader.app.features.chart.feed.ChartFeedRenderer
import com.tradingapp.metatrader.app.features.chart.indicators.IndicatorConfig
import com.tradingapp.metatrader.app.features.chart.indicators.IndicatorController
import com.tradingapp.metatrader.app.features.chart.indicators.ui.IndicatorSettingsBottomSheet
import com.tradingapp.metatrader.app.features.chart.markers.ChartMarkerJson
import com.tradingapp.metatrader.app.features.chart.markers.live.LiveMarkerBus
import com.tradingapp.metatrader.app.features.chart.market.ChartMarketController
import com.tradingapp.metatrader.app.features.chart.webview.ChartWebView
import com.tradingapp.metatrader.domain.repository.DrawingRepository
import com.tradingapp.metatrader.app.features.drawing.store.DrawingStore
import com.tradingapp.metatrader.app.features.drawing.ui.DrawingOverlayView
import com.tradingapp.metatrader.app.features.expert.data.ExpertAttachmentRepository
import com.tradingapp.metatrader.app.features.expert.data.ExpertScriptRepository
import com.tradingapp.metatrader.app.features.expert.inputs.ExpertInputsStore
import com.tradingapp.metatrader.app.features.expert.ui.ExpertsActivity
import com.tradingapp.metatrader.app.features.oanda.settings.ui.OandaSettingsActivity
import com.tradingapp.metatrader.app.features.replay.ReplayCandleFeed
import com.tradingapp.metatrader.app.features.replay.ReplaySpeed
import com.tradingapp.metatrader.app.features.sessions.ui.ChartSessionsActivity
import com.tradingapp.metatrader.app.features.tester.visual.VisualModeSessionMt5
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class ChartActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SYMBOL = "symbol"
        const val EXTRA_TIMEFRAME = "timeframe"
        const val EXTRA_VISUAL_SCRIPT = "visual_script"
        const val EXTRA_VISUAL_AUTOSTART = "visual_autostart"
    }

    private val vm: ChartViewModel by viewModels()

    @Inject lateinit var scripts: ExpertScriptRepository
    @Inject lateinit var attachments: ExpertAttachmentRepository
    @Inject lateinit var inputsStore: ExpertInputsStore
    @Inject lateinit var markerBus: LiveMarkerBus

    @Inject lateinit var autoTradingStore: AutoTradingStore
    @Inject lateinit var autoTradingOrchestrator: AutoTradingOrchestrator

    @Inject lateinit var oandaSettingsStore: OandaSettingsStore
    @Inject lateinit var candleCache: CandleCacheRepository

    @Inject lateinit var replayFeed: ReplayCandleFeed

    @Inject lateinit var drawingStore: DrawingStore
    @Inject lateinit var drawingRepo: DrawingRepository

    @Inject lateinit var bus: OrderCommandBus

    private lateinit var controller: ChartMarketController
    private var replayJob: Job? = null

    private lateinit var web: ChartWebView
    private lateinit var renderer: ChartFeedRenderer
    private lateinit var indicators: IndicatorController
    private lateinit var overlay: DrawingOverlayView

    private var autosaveJob: Job? = null

    private var indicatorConfig: IndicatorConfig = IndicatorConfig(
        emaPeriods = listOf(20, 50),
        stochK = 14,
        stochD = 3
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        intent.getStringExtra(EXTRA_SYMBOL)?.let { vm.setSymbol(it) }
        intent.getStringExtra(EXTRA_TIMEFRAME)?.let { vm.setTimeframe(it) }

        autoTradingOrchestrator.startWatching()

        val titleText: TextView = findViewById(R.id.titleText)
        val subText: TextView = findViewById(R.id.subText)
        val statusText: TextView = findViewById(R.id.statusText)

        val attachBtn: Button = findViewById(R.id.attachBtn)
        val propsBtn: Button = findViewById(R.id.propsBtn)
        val orderBtn: Button = findViewById(R.id.orderBtn)
        val autoBtn: Button = findViewById(R.id.autoBtn)

        val connectBtn: Button = findViewById(R.id.connectBtn)
        val replayBtn: Button = findViewById(R.id.replayBtn)
        val chartsBtn: Button = findViewById(R.id.chartsBtn)
        val journalBtn: Button = findViewById(R.id.journalBtn)
        val expertsBtn: Button = findViewById(R.id.expertsBtn)
        val oandaBtn: Button = findViewById(R.id.oandaBtn)

        val toolNoneBtn: Button = findViewById(R.id.toolNoneBtn)
        val toolTrendBtn: Button = findViewById(R.id.toolTrendBtn)
        val toolHLineBtn: Button = findViewById(R.id.toolHLineBtn)
        val toolMoveBtn: Button = findViewById(R.id.toolMoveBtn)
        val indicatorsBtn: Button = findViewById(R.id.indicatorsBtn)

        web = findViewById(R.id.chartWebView)
        web.initChart()

        overlay = findViewById(R.id.drawingOverlay)
        overlay.bind(web, drawingStore)

        indicators = IndicatorController(web, indicatorConfig)

        renderer = ChartFeedRenderer(
            web = web,
            onStatus = { msg -> runOnUiThread { statusText.text = "Status: $msg" } },
            onAfterApply = { upd -> indicators.onUpdate(upd) }
        )

        controller = ChartMarketController(
            settingsStore = oandaSettingsStore,
            cache = candleCache,
            scope = lifecycleScope,
            webView = web,
            onStatus = { msg -> runOnUiThread { statusText.text = "Status: $msg" } },
            renderer = renderer
        )

        toolNoneBtn.setOnClickListener { overlay.mode = DrawingOverlayView.Mode.NONE }
        toolTrendBtn.setOnClickListener { overlay.mode = DrawingOverlayView.Mode.DRAW_TREND }
        toolHLineBtn.setOnClickListener { overlay.mode = DrawingOverlayView.Mode.DRAW_HLINE }
        toolMoveBtn.setOnClickListener { overlay.mode = DrawingOverlayView.Mode.MOVE }

        indicatorsBtn.setOnClickListener {
            IndicatorSettingsBottomSheet(indicatorConfig) { newCfg ->
                indicatorConfig = newCfg
                indicators.setConfig(newCfg)
            }.show(supportFragmentManager, "indicators")
        }

        orderBtn.setOnClickListener { showPlacePendingDialog() }

        chartsBtn.setOnClickListener { startActivity(Intent(this, ChartSessionsActivity::class.java)) }
        expertsBtn.setOnClickListener { startActivity(Intent(this, ExpertsActivity::class.java)) }
        journalBtn.setOnClickListener { startActivity(Intent(this, LiveJournalActivity::class.java)) }
        oandaBtn.setOnClickListener { startActivity(Intent(this, OandaSettingsActivity::class.java)) }

        attachBtn.setOnClickListener { showAttachDialog() }
        propsBtn.setOnClickListener { showInputsDialogIfAttached() }
        autoBtn.setOnClickListener { lifecycleScope.launch { autoTradingStore.toggle() } }

        connectBtn.setOnClickListener {
            val st = vm.state.value
            stopReplayIfRunning(replayBtn)
            if (!controller.isConnected()) {
                connectBtn.text = "Disconnect"
                controller.connect(symbol = st.symbol, timeframe = st.timeframe)
            } else {
                controller.disconnect()
                connectBtn.text = "Connect"
            }
        }

        replayBtn.setOnClickListener {
            val st = vm.state.value
            if (replayJob != null) {
                stopReplayIfRunning(replayBtn)
                return@setOnClickListener
            }
            if (controller.isConnected()) {
                controller.disconnect()
                connectBtn.text = "Connect"
            }
            chooseReplaySpeedAndStart(st.symbol, st.timeframe, replayBtn)
        }

        lifecycleScope.launchWhenStarted {
            vm.state.collectLatest { st ->
                titleText.text = "${st.symbol} • ${st.timeframe}"
                val eaName = st.attachedScriptName ?: "(none)"
                val at = if (st.autoTradingOn) "ON" else "OFF"
                subText.text = "AutoTrading: $at | EA: $eaName"
                propsBtn.isEnabled = (st.attachedScriptId != null)

                overlay.symbol = st.symbol
                overlay.timeframe = st.timeframe

                lifecycleScope.launch {
                    val items = drawingRepo.load(st.symbol, st.timeframe)
                    drawingStore.setAll(items)
                    overlay.invalidate()
                }

                autosaveJob?.cancel()
                autosaveJob = lifecycleScope.launch {
                    drawingStore.items.collectLatest { list ->
                        delay(350)
                        drawingRepo.saveSnapshot(st.symbol, st.timeframe, list)
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            autoTradingStore.enabledFlow.collectLatest { enabled ->
                autoBtn.text = if (enabled) "Auto: ON" else "Auto: OFF"
            }
        }

        lifecycleScope.launchWhenStarted {
            markerBus.flow.collectLatest { marker ->
                web.addMarkerJson(ChartMarkerJson.toJsonObj(marker))
            }
        }

        val script = intent.getStringExtra(EXTRA_VISUAL_SCRIPT).orEmpty()
        val autoStart = intent.getBooleanExtra(EXTRA_VISUAL_AUTOSTART, false)
        if (autoStart && script.isNotBlank()) {
            if (controller.isConnected()) {
                controller.disconnect()
                connectBtn.text = "Connect"
            }
            replayFeed.speed = ReplaySpeed.X4
            startVisualMode(vm.state.value.symbol, vm.state.value.timeframe, script, replayBtn)
        }
    }

    override fun onStop() {
        super.onStop()
        stopReplayIfRunning(findViewById(R.id.replayBtn))
        if (controller.isConnected()) {
            controller.disconnect()
            findViewById<Button>(R.id.connectBtn).text = "Connect"
        }
    }

    private fun showPlacePendingDialog() {
        val st = vm.state.value
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_place_pending, null, false)
        val typeSpinner: Spinner = view.findViewById(R.id.typeSpinner)
        val lotsEdit: EditText = view.findViewById(R.id.lotsEdit)
        val entryEdit: EditText = view.findViewById(R.id.entryEdit)
        val slEdit: EditText = view.findViewById(R.id.slEdit)
        val tpEdit: EditText = view.findViewById(R.id.tpEdit)
        val commentEdit: EditText = view.findViewById(R.id.commentEdit)

        val types = PendingType.values().map { it.name }
        typeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)

        AlertDialog.Builder(this)
            .setTitle("Pending Order • ${st.symbol} ${st.timeframe}")
            .setView(view)
            .setPositiveButton("Place") { _, _ ->
                val type = PendingType.valueOf(types[typeSpinner.selectedItemPosition])
                val lots = lotsEdit.text.toString().trim().toDoubleOrNull() ?: 0.0
                val entry = entryEdit.text.toString().trim().toDoubleOrNull() ?: Double.NaN
                val sl = slEdit.text.toString().trim().toDoubleOrNull()
                val tp = tpEdit.text.toString().trim().toDoubleOrNull()
                val comment = commentEdit.text.toString().trim().ifEmpty { null }

                if (lots <= 0.0 || !entry.isFinite()) {
                    AlertDialog.Builder(this)
                        .setTitle("Invalid Input")
                        .setMessage("Lots and entry price are required.")
                        .setPositiveButton("OK", null)
                        .show()
                    return@setPositiveButton
                }

                bus.tryEmit(
                    OrderCommand.PlacePending(
                        symbol = st.symbol,
                        timeframe = st.timeframe,
                        type = type,
                        lots = lots,
                        entryPrice = entry,
                        sl = sl,
                        tp = tp,
                        comment = comment
                    )
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startVisualMode(symbol: String, timeframe: String, scriptText: String, replayBtn: Button) {
        stopReplayIfRunning(replayBtn)
        replayBtn.text = "Stop Replay"

        val session = VisualModeSessionMt5(
            replayFeed = replayFeed,
            renderer = renderer,
            addMarkerJson = { json -> web.addMarkerJson(json) },
            onStatus = { msg -> runOnUiThread { findViewById<TextView>(R.id.statusText).text = "Status: $msg" } }
        )

        replayJob = lifecycleScope.launch {
            try {
                session.run(symbol, timeframe, scriptText)
            } catch (t: Throwable) {
                runOnUiThread { findViewById<TextView>(R.id.statusText).text = "Status: Visual error: ${t.message}" }
            } finally {
                runOnUiThread { replayBtn.text = "Replay" }
                replayJob = null
            }
        }
    }

    private fun chooseReplaySpeedAndStart(symbol: String, timeframe: String, replayBtn: Button) {
        val speeds = ReplaySpeed.values().map { it.name }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Replay Speed")
            .setItems(speeds) { _, which ->
                val sp = ReplaySpeed.values().getOrNull(which) ?: ReplaySpeed.X4
                replayFeed.speed = sp
                startReplay(symbol, timeframe, replayBtn)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startReplay(symbol: String, timeframe: String, replayBtn: Button) {
        replayBtn.text = "Stop Replay"
        replayJob = lifecycleScope.launch {
            replayFeed.stream(symbol, timeframe).collect { upd ->
                renderer.apply(upd)
            }
        }
    }

    private fun stopReplayIfRunning(replayBtn: Button) {
        replayJob?.cancel()
        replayJob = null
        replayBtn.text = "Replay"
    }

    private fun showAttachDialog() {
        val st = vm.state.value
        val list = scripts.getAll()
        if (list.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("No Experts")
                .setMessage("Create an Expert first in Experts screen.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_select_ea, null, false)
        val sp: Spinner = view.findViewById(R.id.scriptSpinner)
        sp.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, list.map { it.name })

        AlertDialog.Builder(this)
            .setTitle("Attach EA to ${st.symbol} ${st.timeframe}")
            .setView(view)
            .setPositiveButton("Attach") { _, _ ->
                val idx = sp.selectedItemPosition.coerceIn(0, list.lastIndex)
                val script = list[idx]
                lifecycleScope.launch {
                    attachments.attach(scriptId = script.id, symbol = st.symbol, timeframe = st.timeframe)
                    AlertDialog.Builder(this@ChartActivity)
                        .setMessage("Attached '${script.name}' to ${st.symbol} ${st.timeframe}")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showInputsDialogIfAttached() {
        val st = vm.state.value
        val scriptId = st.attachedScriptId ?: return

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_inputs, null, false)
        val edit: EditText = view.findViewById(R.id.inputsEdit)

        lifecycleScope.launch {
            val current = runCatching { inputsStore.getInputsJson(scriptId) }.getOrElse { "{}" }
            edit.setText(current)

            AlertDialog.Builder(this@ChartActivity)
                .setTitle("EA Properties: ${st.attachedScriptName ?: ""}")
                .setView(view)
                .setPositiveButton("Save") { _, _ ->
                    val txt = edit.text.toString().trim()
                    val ok = runCatching { JSONObject(txt) }.isSuccess
                    if (!ok) {
                        AlertDialog.Builder(this@ChartActivity)
                            .setTitle("Invalid JSON")
                            .setMessage("Please provide valid JSON object.")
                            .setPositiveButton("OK", null)
                            .show()
                        return@setPositiveButton
                    }

                    lifecycleScope.launch {
                        inputsStore.setInputsJson(scriptId, txt)
                        AlertDialog.Builder(this@ChartActivity)
                            .setMessage("Inputs saved.")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
