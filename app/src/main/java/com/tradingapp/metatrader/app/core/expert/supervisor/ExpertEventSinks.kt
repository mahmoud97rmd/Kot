package com.tradingapp.metatrader.app.core.expert.supervisor

import com.tradingapp.metatrader.app.core.expert.runtime.ExpertRuntime

interface ExpertLogSink {
    fun log(text: String)
}

interface ExpertMarkerSink {
    fun onBuy(timeSec: Long, text: String)
    fun onSell(timeSec: Long, text: String)
    fun onClose(timeSec: Long, text: String)
}

object ExpertEventRouter {
    fun route(e: ExpertRuntime.Event, log: ExpertLogSink, markers: ExpertMarkerSink) {
        log.log("EA Event: ${e.type} ${e.message} @${e.timeSec}")
        when {
            e.message.startsWith("BUY ") -> markers.onBuy(e.timeSec, "BUY")
            e.message.startsWith("SELL ") -> markers.onSell(e.timeSec, "SELL")
            e.type == "CLOSE" -> markers.onClose(e.timeSec, "CLOSE")
        }
    }
}
