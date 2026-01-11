package com.tradingapp.metatrader.app.features.chart.markers

import com.tradingapp.metatrader.domain.models.backtest.BacktestResult

object BacktestResultMarkerMapper {

    fun map(result: BacktestResult): List<ChartMarker> {
        val out = ArrayList<ChartMarker>(result.trades.size * 2)

        for (t in result.trades) {
            val isBuy = t.side.uppercase() == "BUY"

            // entry marker
            out.add(
                ChartMarker(
                    timeSec = t.entryTimeSec,
                    position = if (isBuy) "belowBar" else "aboveBar",
                    color = if (isBuy) "#26a69a" else "#ef5350",
                    shape = if (isBuy) "arrowUp" else "arrowDown",
                    text = "${t.side} @ ${trim(t.entryPrice)}"
                )
            )

            // exit marker
            out.add(
                ChartMarker(
                    timeSec = t.exitTimeSec,
                    position = "aboveBar",
                    color = if (t.profit >= 0.0) "#4caf50" else "#ff5252",
                    shape = "circle",
                    text = "Exit ${t.reason} PnL=${trim(t.profit)}"
                )
            )
        }

        return out.sortedBy { it.timeSec }
    }

    private fun trim(x: Double): String = String.format(java.util.Locale.US, "%.2f", x)
}
