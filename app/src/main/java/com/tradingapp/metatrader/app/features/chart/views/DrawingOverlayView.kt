package com.tradingapp.metatrader.app.features.chart.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

/**
 * طبقة رسم شفافة فوق WebView.
 * لاحقًا سنضيف أدوات: Trendline, Horizontal, Fibonacci...
 */
class DrawingOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF4FC3F7.toInt()
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    var drawingEnabled: Boolean = false

    private var x1 = 0f
    private var y1 = 0f
    private var x2 = 0f
    private var y2 = 0f
    private var hasLine = false

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (hasLine) {
            canvas.drawLine(x1, y1, x2, y2, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!drawingEnabled) return false // مرر اللمس للـ WebView

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                x1 = event.x; y1 = event.y
                x2 = x1; y2 = y1
                hasLine = true
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                x2 = event.x; y2 = event.y
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                x2 = event.x; y2 = event.y
                invalidate()
                return true
            }
        }
        return true
    }
}
