package com.tradingapp.metatrader.app.features.chart.indicators

import com.tradingapp.metatrader.app.core.candles.Candle
import com.tradingapp.metatrader.app.core.feed.CandleUpdate
import com.tradingapp.metatrader.app.core.indicators.EMACalculator
import com.tradingapp.metatrader.app.core.indicators.StochasticCalculator
import com.tradingapp.metatrader.app.features.chart.webview.ChartWebView
import org.json.JSONArray
import org.json.JSONObject

class IndicatorController(
    private val web: ChartWebView,
    private var config: IndicatorConfig = IndicatorConfig()
) {
    private val emaCalcs = linkedMapOf<Int, EMACalculator>()
    private var stochCalc = StochasticCalculator(config.stochK, config.stochD)

    fun setConfig(newConfig: IndicatorConfig) {
        config = newConfig
        reset()
    }

    fun reset() {
        emaCalcs.clear()
        for (p in config.emaPeriods.distinct().filter { it > 0 }) {
            emaCalcs[p] = EMACalculator(p)
        }
        stochCalc = StochasticCalculator(config.stochK, config.stochD)

        web.evalJs("window.clearIndicators && window.clearIndicators();")
        // Create EMA series now
        for (p in emaCalcs.keys) {
            val color = if (p == 20) "#2962FF" else "#ab47bc"
            web.evalJs("window.ensureEma && window.ensureEma(${p}, '${color}');")
        }
    }

    fun onUpdate(upd: CandleUpdate) {
        when (upd) {
            is CandleUpdate.History -> applyHistory(upd.candles)
            is CandleUpdate.Current -> applyCurrent(upd.candle)
            else -> {}
        }
    }

    private fun applyHistory(candles: List<Candle>) {
        if (candles.isEmpty()) return
        reset()

        // EMA histories
        for ((period, calc) in emaCalcs) {
            val arr = JSONArray()
            for (c in candles) {
                val ema = calc.update(c.close)
                arr.put(JSONObject().apply {
                    put("time", c.timeSec)
                    put("value", ema)
                })
            }
            val js = "window.setEmaHistory && window.setEmaHistory(${period}, ${arr.toString()});"
            web.evalJs(js)
        }

        // Stoch history
        val kArr = JSONArray()
        val dArr = JSONArray()
        for (c in candles) {
            val v = stochCalc.update(c) ?: continue
            kArr.put(JSONObject().apply { put("time", c.timeSec); put("value", v.k) })
            dArr.put(JSONObject().apply { put("time", c.timeSec); put("value", v.d) })
        }
        web.evalJs("window.setStochHistory && window.setStochHistory(${kArr.toString()}, ${dArr.toString()});")
    }

    private fun applyCurrent(c: Candle) {
        // EMA update point
        for ((period, calc) in emaCalcs) {
            val ema = calc.update(c.close)
            val point = JSONObject().apply { put("time", c.timeSec); put("value", ema) }
            web.evalJs("window.updateEmaPoint && window.updateEmaPoint(${period}, ${point.toString()});")
        }

        val v = stochCalc.update(c)
        if (v != null) {
            val kp = JSONObject().apply { put("time", c.timeSec); put("value", v.k) }
            val dp = JSONObject().apply { put("time", c.timeSec); put("value", v.d) }
            web.evalJs("window.updateStochPoint && window.updateStochPoint(${kp.toString()}, ${dp.toString()});")
        }
    }
}
