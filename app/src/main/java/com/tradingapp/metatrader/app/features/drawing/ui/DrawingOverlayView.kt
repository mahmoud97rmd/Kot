package com.tradingapp.metatrader.app.features.drawing.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.tradingapp.metatrader.app.features.chart.webview.ChartWebView
import com.tradingapp.metatrader.app.features.drawing.model.Anchor
import com.tradingapp.metatrader.app.features.drawing.model.HorizontalLine
import com.tradingapp.metatrader.app.features.drawing.model.ScreenMapper
import com.tradingapp.metatrader.app.features.drawing.model.TrendLine
import com.tradingapp.metatrader.app.features.drawing.store.DrawingStore
import org.json.JSONObject
import kotlin.math.abs

class DrawingOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    enum class Mode { NONE, DRAW_TREND, DRAW_HLINE, MOVE }

    var symbol: String = "XAU_USD"
    var timeframe: String = "M1"

    private var web: ChartWebView? = null
    private var store: DrawingStore? = null

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#d1d4dc")
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }
    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#ffcc00")
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }
    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#ffcc00")
        style = Paint.Style.FILL
    }

    var mode: Mode = Mode.NONE
        set(value) {
            field = value
            tempA = null
            selectedId = null
            invalidate()
        }

    private var tempA: Anchor? = null
    private var selectedId: String? = null
    private var moveTarget: MoveTarget? = null

    private var downTimeMs = 0L

    fun bind(webView: ChartWebView, drawingStore: DrawingStore) {
        this.web = webView
        this.store = drawingStore
        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val mapper = ScreenMapper(web?.bridge?.viewport, width, height)

        val items = store?.items?.value ?: emptyList()
        for (obj in items) {
            val paint = if (obj.id == selectedId) selectedPaint else linePaint
            when (obj) {
                is TrendLine -> {
                    val p1 = mapper.toScreen(obj.a)
                    val p2 = mapper.toScreen(obj.b)
                    if (p1 != null && p2 != null) {
                        canvas.drawLine(p1.first, p1.second, p2.first, p2.second, paint)
                        if (obj.id == selectedId) {
                            canvas.drawCircle(p1.first, p1.second, 10f, handlePaint)
                            canvas.drawCircle(p2.first, p2.second, 10f, handlePaint)
                        }
                    }
                }
                is HorizontalLine -> {
                    val y = mapper.priceToY(obj.price)
                    if (y != null) canvas.drawLine(0f, y, width.toFloat(), y, paint)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val webView = web ?: return false
        val drawingStore = store ?: return false

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downTimeMs = System.currentTimeMillis()

                if (mode == Mode.NONE) {
                    trySelectAt(event.x, event.y, drawingStore)
                    return selectedId != null
                }

                if (mode == Mode.MOVE) {
                    trySelectAt(event.x, event.y, drawingStore)
                    if (selectedId == null) return false
                    moveTarget = buildMoveTarget(event.x, event.y, drawingStore)
                    return true
                }

                webView.requestCoordToValue(event.x, event.y) { json ->
                    val o = JSONObject(json)
                    if (o.has("error")) return@requestCoordToValue
                    val t = o.optDouble("time", Double.NaN)
                    val p = o.optDouble("price", Double.NaN)
                    if (!t.isFinite() || !p.isFinite()) return@requestCoordToValue
                    post { onDownAnchor(Anchor(timeSec = t.toLong(), price = p), drawingStore) }
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (mode == Mode.MOVE && selectedId != null && moveTarget != null) {
                    webView.requestCoordToValue(event.x, event.y) { json ->
                        val o = JSONObject(json)
                        if (o.has("error")) return@requestCoordToValue
                        val t = o.optDouble("time", Double.NaN)
                        val p = o.optDouble("price", Double.NaN)
                        if (!t.isFinite() || !p.isFinite()) return@requestCoordToValue
                        post { applyMoveTarget(t.toLong(), p, drawingStore) }
                    }
                    return true
                }
                return (mode != Mode.NONE)
            }

            MotionEvent.ACTION_UP -> {
                val pressMs = System.currentTimeMillis() - downTimeMs
                if (mode == Mode.NONE && pressMs > 650 && selectedId != null) {
                    drawingStore.removeById(selectedId!!)
                    selectedId = null
                    invalidate()
                    return true
                }
                moveTarget = null
                return (mode != Mode.NONE) || (selectedId != null)
            }
        }
        return false
    }

    private fun onDownAnchor(anchor: Anchor, store: DrawingStore) {
        when (mode) {
            Mode.DRAW_TREND -> {
                val a = tempA
                if (a == null) tempA = anchor
                else {
                    store.add(TrendLine(symbol = symbol, timeframe = timeframe, a = a, b = anchor))
                    tempA = null
                }
                invalidate()
            }
            Mode.DRAW_HLINE -> {
                store.add(HorizontalLine(symbol = symbol, timeframe = timeframe, price = anchor.price))
                invalidate()
            }
            else -> {}
        }
    }

    private fun trySelectAt(x: Float, y: Float, store: DrawingStore) {
        val mapper = ScreenMapper(web?.bridge?.viewport, width, height)
        val tol = 18f
        val items = store.items.value
        val found = items.asReversed().firstOrNull { it.hitTest(x, y, mapper, tol) }
        selectedId = found?.id
        invalidate()
    }

    private sealed class MoveTarget {
        data class TrendPoint(val id: String, val which: Int) : MoveTarget()
        data class HLine(val id: String) : MoveTarget()
    }

    private fun buildMoveTarget(x: Float, y: Float, store: DrawingStore): MoveTarget? {
        val id = selectedId ?: return null
        val obj = store.items.value.firstOrNull { it.id == id } ?: return null
        val mapper = ScreenMapper(web?.bridge?.viewport, width, height)

        return when (obj) {
            is TrendLine -> {
                val p1 = mapper.toScreen(obj.a) ?: return MoveTarget.TrendPoint(id, 1)
                val p2 = mapper.toScreen(obj.b) ?: return MoveTarget.TrendPoint(id, 2)
                val d1 = abs(x - p1.first) + abs(y - p1.second)
                val d2 = abs(x - p2.first) + abs(y - p2.second)
                if (d1 <= d2) MoveTarget.TrendPoint(id, 1) else MoveTarget.TrendPoint(id, 2)
            }
            is HorizontalLine -> MoveTarget.HLine(id)
        }
    }

    private fun applyMoveTarget(timeSec: Long, price: Double, store: DrawingStore) {
        val t = moveTarget ?: return
        val id = selectedId ?: return
        val obj = store.items.value.firstOrNull { it.id == id } ?: return

        when (t) {
            is MoveTarget.TrendPoint -> {
                val tl = obj as? TrendLine ?: return
                if (t.which == 1) { tl.a.timeSec = timeSec; tl.a.price = price }
                else { tl.b.timeSec = timeSec; tl.b.price = price }
                store.update(tl)
            }
            is MoveTarget.HLine -> {
                val hl = obj as? HorizontalLine ?: return
                hl.price = price
                store.update(hl)
            }
        }
        invalidate()
    }
}
