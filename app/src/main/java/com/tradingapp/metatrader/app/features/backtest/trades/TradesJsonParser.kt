package com.tradingapp.metatrader.app.features.backtest.trades

import org.json.JSONArray

object TradesJsonParser {
    fun parse(json: String): List<UiTrade> {
        val arr = runCatching { JSONArray(json) }.getOrNull() ?: return emptyList()
        val out = ArrayList<UiTrade>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                UiTrade(
                    id = o.optString("id"),
                    side = o.optString("side"),
                    entryTimeSec = o.optLong("entryTimeSec"),
                    exitTimeSec = o.optLong("exitTimeSec"),
                    entryPrice = o.optDouble("entryPrice"),
                    exitPrice = o.optDouble("exitPrice"),
                    profit = o.optDouble("profit"),
                    reason = o.optString("reason")
                )
            )
        }
        return out
    }
}
