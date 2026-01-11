package com.tradingapp.metatrader.app.features.chart.markers

import org.json.JSONObject

/**
 * Lightweight marker JSON generator for Lightweight Charts.
 * Your JS should accept marker objects with:
 * { time: <unixSec>, position: "aboveBar"|"belowBar", color: "#...", shape:"arrowUp"|"arrowDown"|"circle", text:"..." }
 */
object ChartMarkerJson {

    fun tradeMarker(timeSec: Long, text: String): String {
        val o = JSONObject()
        o.put("time", timeSec)
        o.put("position", "aboveBar")
        o.put("color", "#e0b400")
        o.put("shape", "circle")
        o.put("text", text)
        return o.toString()
    }

    /**
     * If you already have a marker model, keep using it.
     * This is a safe fallback adapter (expects JSONObject-like map keys).
     */
    fun toJsonObj(any: Any): String {
        return when (any) {
            is String -> any
            is JSONObject -> any.toString()
            else -> JSONObject().put("text", any.toString()).toString()
        }
    }
}
