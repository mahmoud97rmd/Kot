package com.tradingapp.metatrader.app.features.drawing.model

import com.tradingapp.metatrader.app.features.chart.bridge.ChartBridge

/**
 * Maps (timeSec, price) <-> (xPx, yPx) using viewport reported by JS.
 * Assumes linear mapping across view dimensions.
 */
class ScreenMapper(
    private val viewport: ChartBridge.Viewport?,
    private val widthPx: Int,
    private val heightPx: Int
) {
    fun timeToX(timeSec: Long): Float? {
        val vp = viewport ?: return null
        val minT = vp.minTime
        val maxT = vp.maxTime
        val denom = (maxT - minT)
        if (denom == 0.0) return null
        val x = ( (timeSec.toDouble() - minT) / denom ) * widthPx.toDouble()
        return x.toFloat()
    }

    fun priceToY(price: Double): Float? {
        val vp = viewport ?: return null
        val minP = vp.minPrice
        val maxP = vp.maxPrice
        val denom = (maxP - minP)
        if (denom == 0.0) return null
        // y=0 top; price increases upward => invert
        val y = ( (maxP - price) / denom ) * heightPx.toDouble()
        return y.toFloat()
    }

    fun toScreen(a: Anchor): Pair<Float, Float>? {
        val x = timeToX(a.timeSec) ?: return null
        val y = priceToY(a.price) ?: return null
        return Pair(x, y)
    }
}
