package com.tradingapp.metatrader.core.backtest.io

import com.tradingapp.metatrader.domain.models.backtest.BacktestCandle

object CsvBacktestCandleParser {

    /**
     * Expected header:
     * timeSec,open,high,low,close
     */
    fun parse(csvText: String): List<BacktestCandle> {
        val lines = csvText
            .split('\n')
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (lines.isEmpty()) return emptyList()

        // Skip header if contains non-numeric tokens
        val startIndex = if (lines[0].lowercase().contains("timesec")) 1 else 0

        val out = ArrayList<BacktestCandle>(maxOf(0, lines.size - startIndex))

        for (i in startIndex until lines.size) {
            val line = lines[i]
            val parts = line.split(',')
                .map { it.trim() }

            if (parts.size < 5) continue

            val t = parts[0].toLongOrNull() ?: continue
            val o = parts[1].toDoubleOrNull() ?: continue
            val h = parts[2].toDoubleOrNull() ?: continue
            val l = parts[3].toDoubleOrNull() ?: continue
            val c = parts[4].toDoubleOrNull() ?: continue

            out.add(
                BacktestCandle(
                    timeSec = t,
                    open = o,
                    high = h,
                    low = l,
                    close = c
                )
            )
        }

        return out
    }
}
