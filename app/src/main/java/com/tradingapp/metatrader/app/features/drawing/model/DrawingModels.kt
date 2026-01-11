package com.tradingapp.metatrader.app.features.drawing.model

import java.util.UUID
import kotlin.math.abs

data class Anchor(
    var timeSec: Long,
    var price: Double
)

sealed class DrawingObject(
    val id: String,
    val symbol: String,
    val timeframe: String
) {
    companion object {
        fun newId(): String = UUID.randomUUID().toString()
    }
    abstract fun hitTest(x: Float, y: Float, mapper: ScreenMapper, tolPx: Float): Boolean
}

class TrendLine(
    id: String = DrawingObject.newId(),
    symbol: String,
    timeframe: String,
    val a: Anchor,
    val b: Anchor
) : DrawingObject(id = id, symbol = symbol, timeframe = timeframe) {

    override fun hitTest(x: Float, y: Float, mapper: ScreenMapper, tolPx: Float): Boolean {
        val p1 = mapper.toScreen(a) ?: return false
        val p2 = mapper.toScreen(b) ?: return false
        return distancePointToSegment(x, y, p1.first, p1.second, p2.first, p2.second) <= tolPx
    }
}

class HorizontalLine(
    id: String = DrawingObject.newId(),
    symbol: String,
    timeframe: String,
    var price: Double
) : DrawingObject(id = id, symbol = symbol, timeframe = timeframe) {

    override fun hitTest(x: Float, y: Float, mapper: ScreenMapper, tolPx: Float): Boolean {
        val yLine = mapper.priceToY(price) ?: return false
        return abs(y - yLine) <= tolPx
    }
}

private fun distancePointToSegment(px: Float, py: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val vx = x2 - x1
    val vy = y2 - y1
    val wx = px - x1
    val wy = py - y1

    val c1 = wx * vx + wy * vy
    if (c1 <= 0f) return hypot(px - x1, py - y1)
    val c2 = vx * vx + vy * vy
    if (c2 <= c1) return hypot(px - x2, py - y2)
    val b = c1 / c2
    val bx = x1 + b * vx
    val by = y1 + b * vy
    return hypot(px - bx, py - by)
}

private fun hypot(a: Float, b: Float): Float = kotlin.math.sqrt(a * a + b * b)
