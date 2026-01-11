package com.tradingapp.metatrader.app.features.chart.json

import com.tradingapp.metatrader.app.core.market.MarketCandle
import org.json.JSONArray
import org.json.JSONObject

object ChartCandleJson {

    fun candlesToJsonArray(candles: List<MarketCandle>): String {
        val arr = JSONArray()
        for (c in candles) {
            arr.put(candleToJsonObject(c))
        }
        return arr.toString()
    }

    fun candleToJsonObject(c: MarketCandle): String {
        val o = JSONObject()
        o.put("time", c.timeSec)
        o.put("open", c.open)
        o.put("high", c.high)
        o.put("low", c.low)
        o.put("close", c.close)
        return o.toString()
    }
}
