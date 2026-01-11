package com.tradingapp.metatrader.app.features.backtest.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

class EquityCurveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF0B1220.toInt()
        style = Paint.Style.FILL
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF4DA3FF.toInt()
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF1F2A44.toInt()
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private var points: List<Double> = emptyList()

    fun setEquityCurve(curve: List<Double>) {
        points = curve
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // grid
        val w = width.toFloat()
        val h = height.toFloat()
        for (i in 1..3) {
            val y = (h / 4f) * i
            canvas.drawLine(0f, y, w, y, gridPaint)
        }

        if (points.size < 2) return

        var minV = Double.POSITIVE_INFINITY
        var maxV = Double.NEGATIVE_INFINITY
        for (v in points) {
            minV = min(minV, v)
            maxV = max(maxV, v)
        }
        val range = (maxV - minV).takeIf { it > 0.0000001 } ?: 1.0

        val path = Path()
        for (i in points.indices) {
            val x = (i.toFloat() / (points.size - 1).toFloat()) * w
            val yNorm = ((points[i] - minV) / range).toFloat()
            val y = h - (yNorm * h)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        canvas.drawPath(path, linePaint)
    }
}
