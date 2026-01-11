package com.tradingapp.metatrader.app.features.backtest.ui.marker

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.tradingapp.metatrader.app.R
import com.tradingapp.metatrader.domain.models.backtest.EquityPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class EquityMarkerView(
    context: Context,
    private val equityCurve: List<EquityPoint>
) : MarkerView(context, R.layout.marker_equity) {

    private val title: TextView = findViewById(R.id.markerTitle)
    private val body: TextView = findViewById(R.id.markerBody)

    private val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e == null) {
            super.refreshContent(e, highlight)
            return
        }

        val idx = e.x.roundToInt().coerceIn(0, maxOf(0, equityCurve.size - 1))
        val p = equityCurve.getOrNull(idx)

        val timeText = if (p != null) fmt.format(Date(p.timeSec * 1000L)) else "--"
        val eqText = String.format(Locale.US, "%.2f", e.y)

        title.text = "Equity @ $timeText"
        body.text = "Index=$idx | Equity=$eqText"

        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        // يظهر فوق نقطة اللمس
        return MPPointF(-(width / 2f), -height.toFloat())
    }
}
