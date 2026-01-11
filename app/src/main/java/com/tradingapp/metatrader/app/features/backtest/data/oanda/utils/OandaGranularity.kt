package com.tradingapp.metatrader.app.features.backtest.data.oanda.utils

/**
 * OANDA granularity examples: S5, S10, S15, S30, M1, M2, M4, M5, M10, M15, M30, H1, H2, H3, H4, H6, H8, H12, D, W, M
 * (توجد قائمة كاملة في docs instrument endpoint) :contentReference[oaicite:4]{index=4}
 */
object OandaGranularity {

    fun seconds(granularity: String): Long {
        return when (granularity.uppercase()) {
            "S5" -> 5
            "S10" -> 10
            "S15" -> 15
            "S30" -> 30
            "M1" -> 60
            "M2" -> 120
            "M4" -> 240
            "M5" -> 300
            "M10" -> 600
            "M15" -> 900
            "M30" -> 1800
            "H1" -> 3600
            "H2" -> 7200
            "H3" -> 10800
            "H4" -> 14400
            "H6" -> 21600
            "H8" -> 28800
            "H12" -> 43200
            "D" -> 86400
            "W" -> 604800
            "M" -> 2592000 // تقريبي (30 يوم)
            else -> 60
        }
    }
}
