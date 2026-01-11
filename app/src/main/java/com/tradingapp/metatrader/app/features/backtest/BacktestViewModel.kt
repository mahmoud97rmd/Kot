package com.tradingapp.metatrader.app.features.backtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingapp.metatrader.app.features.backtest.expert.RunAttachedExpertBacktestUseCase
import com.tradingapp.metatrader.app.features.backtest.inputs.BacktestInputs
import com.tradingapp.metatrader.app.features.chart.markers.BacktestResultMarkerMapper
import com.tradingapp.metatrader.app.features.chart.markers.ChartMarkerJson
import com.tradingapp.metatrader.domain.models.backtest.BacktestCandle
import com.tradingapp.metatrader.domain.models.backtest.BacktestConfig
import com.tradingapp.metatrader.domain.models.backtest.BacktestResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class BacktestViewModel @Inject constructor(
    private val runAttachedEa: RunAttachedExpertBacktestUseCase
) : ViewModel() {

    data class UiState(
        val instrument: String = "XAU_USD",
        val granularity: String = "M1",
        val rangeFromSec: Long? = null,
        val rangeToSec: Long? = null,

        val inputs: BacktestInputs = BacktestInputs(),

        val running: Boolean = false,
        val progress: String = "Idle",
        val dataSource: String = "Demo",

        val result: BacktestResult? = null,
        val expertLogs: List<String> = emptyList(),

        // Backtest tabs payloads
        val backtestCandlesJson: String = "[]",
        val backtestMarkersJson: String = "[]",
        val equityCurveJson: String = "[]",

        // NEW: trades JSON for Trade List + Export
        val tradesJson: String = "[]"
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun setInstrument(symbol: String) {
        _state.value = _state.value.copy(instrument = symbol)
    }

    fun setGranularity(tf: String) {
        _state.value = _state.value.copy(granularity = tf)
    }

    fun setDateRange(fromSec: Long, toSec: Long) {
        _state.value = _state.value.copy(rangeFromSec = fromSec, rangeToSec = toSec)
    }

    fun updateInputs(inputs: BacktestInputs) {
        _state.value = _state.value.copy(inputs = inputs)
    }

    fun runBacktestFromRoomThenAssetsThenDemo() {
        val candles = makeDemoCandles(count = 1500, startSec = nowSec() - 1500L * 60L, stepSec = 60L)
        _state.value = _state.value.copy(
            dataSource = "Demo",
            result = null,
            expertLogs = emptyList(),
            backtestCandlesJson = candlesToJson(candles),
            backtestMarkersJson = "[]",
            equityCurveJson = "[]",
            tradesJson = "[]",
            progress = "Generated demo candles: ${candles.size}"
        )
    }

    fun runExpertBacktest() {
        viewModelScope.launch {
            val st = _state.value
            _state.value = st.copy(
                running = true,
                progress = "Running attached EA backtest...",
                expertLogs = emptyList(),
                backtestMarkersJson = "[]",
                equityCurveJson = "[]",
                tradesJson = "[]"
            )

            val step = timeframeToSec(st.granularity)
            val candles = makeDemoCandles(
                count = 2000,
                startSec = nowSec() - 2000L * step,
                stepSec = step
            )

            val cfg = BacktestConfig(
                initialBalance = st.inputs.initialBalance,
                pointValue = st.inputs.pointValue,
                commissionPerLot = st.inputs.commissionPerLot,
                spreadPoints = st.inputs.spreadPoints
            )

            val out = runAttachedEa.run(
                symbol = st.instrument,
                timeframe = st.granularity,
                candles = candles,
                config = cfg
            )

            if (!out.ok) {
                _state.value = _state.value.copy(
                    running = false,
                    progress = "EA backtest failed: ${out.message}",
                    result = null,
                    expertLogs = listOf(out.message),
                    backtestCandlesJson = candlesToJson(candles),
                    backtestMarkersJson = "[]",
                    equityCurveJson = "[]",
                    tradesJson = "[]"
                )
                return@launch
            }

            val res = out.result
            val markersJson = if (res != null) {
                val markers = BacktestResultMarkerMapper.map(res)
                ChartMarkerJson.toJsonArray(markers)
            } else "[]"

            val equityJson = if (res != null) equityToJson(res) else "[]"
            val tradesJson = if (res != null) tradesToJson(res) else "[]"

            _state.value = _state.value.copy(
                running = false,
                progress = out.message,
                result = res,
                expertLogs = out.logs,
                backtestCandlesJson = candlesToJson(candles),
                backtestMarkersJson = markersJson,
                equityCurveJson = equityJson,
                tradesJson = tradesJson
            )
        }
    }

    private fun timeframeToSec(tf: String): Long {
        return when (tf.trim().uppercase()) {
            "M1" -> 60
            "M5" -> 300
            "M15" -> 900
            "M30" -> 1800
            "H1" -> 3600
            "H4" -> 14400
            "D1" -> 86400
            else -> 60
        }
    }

    private fun nowSec(): Long = System.currentTimeMillis() / 1000L

    private fun makeDemoCandles(count: Int, startSec: Long, stepSec: Long): List<BacktestCandle> {
        val out = ArrayList<BacktestCandle>(count)
        var t = startSec
        var price = 2000.0 + Random.nextDouble(-5.0, 5.0)

        repeat(count) {
            val drift = Random.nextDouble(-1.5, 1.5)
            val open = price
            val close = (open + drift).coerceAtLeast(0.1)
            val high = maxOf(open, close) + Random.nextDouble(0.0, 1.0)
            val low = minOf(open, close) - Random.nextDouble(0.0, 1.0)

            out.add(
                BacktestCandle(
                    timeSec = t,
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = Random.nextLong(50, 300)
                )
            )

            price = close
            t += stepSec
        }
        return out
    }

    private fun candlesToJson(candles: List<BacktestCandle>): String {
        val arr = JSONArray()
        for (c in candles) {
            val o = JSONObject()
            o.put("time", c.timeSec)
            o.put("open", c.open)
            o.put("high", c.high)
            o.put("low", c.low)
            o.put("close", c.close)
            arr.put(o)
        }
        return arr.toString()
    }

    private fun equityToJson(res: BacktestResult): String {
        val arr = JSONArray()
        val curve = if (res.equityCurve.isNotEmpty()) res.equityCurve
        else listOf(com.tradingapp.metatrader.domain.models.backtest.EquityPoint(timeSec = nowSec(), equity = res.config.initialBalance))

        for (p in curve) {
            val o = JSONObject()
            o.put("time", p.timeSec)
            o.put("value", p.equity)
            arr.put(o)
        }
        return arr.toString()
    }

    private fun tradesToJson(res: BacktestResult): String {
        val arr = JSONArray()
        for (t in res.trades) {
            val o = JSONObject()
            o.put("id", t.id)
            o.put("side", t.side)
            o.put("entryTimeSec", t.entryTimeSec)
            o.put("exitTimeSec", t.exitTimeSec)
            o.put("entryPrice", t.entryPrice)
            o.put("exitPrice", t.exitPrice)
            o.put("profit", t.profit)
            o.put("reason", t.reason)
            arr.put(o)
        }
        return arr.toString()
    }
}
