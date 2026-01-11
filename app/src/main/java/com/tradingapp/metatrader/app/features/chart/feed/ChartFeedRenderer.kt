package com.tradingapp.metatrader.app.features.chart.feed

import com.tradingapp.metatrader.app.core.feed.CandleUpdate
import com.tradingapp.metatrader.app.features.chart.webview.ChartWebView
import org.json.JSONArray
import org.json.JSONObject

class ChartFeedRenderer(
    private val web: ChartWebView,
    private val onStatus: (String) -> Unit = {},
    private val onAfterApply: ((CandleUpdate) -> Unit)? = null
) {
    fun apply(upd: CandleUpdate) {
        when (upd) {
            is CandleUpdate.Status -> onStatus(upd.message)
            is CandleUpdate.History -> {
                val arr = JSONArray()
                for (c in upd.candles) {
                    arr.put(JSONObject().apply {
                        put("time", c.timeSec)
                        put("open", c.open)
                        put("high", c.high)
                        put("low", c.low)
                        put("close", c.close)
                    })
                }
                web.evalJs("window.setHistory && window.setHistory(${arr.toString()});")
            }
            is CandleUpdate.Current -> {
                val o = JSONObject().apply {
                    put("time", upd.candle.timeSec)
                    put("open", upd.candle.open)
                    put("high", upd.candle.high)
                    put("low", upd.candle.low)
                    put("close", upd.candle.close)
                }
                web.evalJs("window.updateLastCandle && window.updateLastCandle(${o.toString()});")
            }
        }
        onAfterApply?.invoke(upd)
    }
}
