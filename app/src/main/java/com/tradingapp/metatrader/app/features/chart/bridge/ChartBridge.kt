package com.tradingapp.metatrader.app.features.chart.bridge

import android.webkit.JavascriptInterface
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class ChartBridge {

    data class Viewport(
        val minTime: Double,
        val maxTime: Double,
        val minPrice: Double,
        val maxPrice: Double
    )

    @Volatile
    var viewport: Viewport? = null
        private set

    private val reqIdGen = AtomicLong(1L)
    private val callbacks = ConcurrentHashMap<Long, (String) -> Unit>()

    fun nextRequestId(cb: (String) -> Unit): Long {
        val id = reqIdGen.getAndIncrement()
        callbacks[id] = cb
        return id
    }

    @JavascriptInterface
    fun onViewport(json: String) {
        // Expect: {minTime,maxTime,minPrice,maxPrice}
        try {
            val o = org.json.JSONObject(json)
            viewport = Viewport(
                minTime = o.getDouble("minTime"),
                maxTime = o.getDouble("maxTime"),
                minPrice = o.getDouble("minPrice"),
                maxPrice = o.getDouble("maxPrice")
            )
        } catch (_: Throwable) {
            // ignore
        }
    }

    @JavascriptInterface
    fun onCoordResult(reqId: String, json: String) {
        val id = reqId.toLongOrNull() ?: return
        val cb = callbacks.remove(id) ?: return
        cb(json)
    }
}
