package com.tradingapp.metatrader.app.features.chart.markers

data class ChartMarker(
    val timeSec: Long,
    val position: String, // "aboveBar" or "belowBar"
    val color: String,    // "#RRGGBB"
    val shape: String,    // "arrowUp","arrowDown","circle"
    val text: String
)
