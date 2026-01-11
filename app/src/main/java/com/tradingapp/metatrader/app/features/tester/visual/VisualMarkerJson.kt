package com.tradingapp.metatrader.app.features.tester.visual

import org.json.JSONObject

object VisualMarkerJson {

    fun buy(timeSec: Long, text: String): String {
        val o = JSONObject()
        o.put("time", timeSec)
        o.put("position", "belowBar")
        o.put("color", "#00C853")
        o.put("shape", "arrowUp")
        o.put("text", text)
        return o.toString()
    }

    fun sell(timeSec: Long, text: String): String {
        val o = JSONObject()
        o.put("time", timeSec)
        o.put("position", "aboveBar")
        o.put("color", "#D50000")
        o.put("shape", "arrowDown")
        o.put("text", text)
        return o.toString()
    }

    fun close(timeSec: Long, text: String): String {
        val o = JSONObject()
        o.put("time", timeSec)
        o.put("position", "aboveBar")
        o.put("color", "#FFD54F")
        o.put("shape", "circle")
        o.put("text", text)
        return o.toString()
    }
}
