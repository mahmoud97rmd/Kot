package com.tradingapp.metatrader.domain.models

import java.time.Duration

enum class Timeframe(val duration: Duration, val oandaGranularity: String) {
    M1(Duration.ofMinutes(1), "M1"),
    M5(Duration.ofMinutes(5), "M5"),
    M15(Duration.ofMinutes(15), "M15"),
    M30(Duration.ofMinutes(30), "M30"),
    H1(Duration.ofHours(1), "H1")
}
