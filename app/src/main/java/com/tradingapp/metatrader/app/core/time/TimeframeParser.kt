package com.tradingapp.metatrader.app.core.time

object TimeframeParser {
    fun toSeconds(tf: String): Long {
        return when (tf.trim().uppercase()) {
            "M1" -> 60
            "M5" -> 300
            "M15" -> 900
            "M30" -> 1800
            "H1" -> 3600
            "H4" -> 14400
            "D1" -> 86400
            else -> 60
        }
    }
}
