package com.tradingapp.metatrader.app.utils

import java.time.DayOfWeek
import java.time.ZonedDateTime

object MarketSessionUtil {

    fun isMarketOpen(now: ZonedDateTime = ZonedDateTime.now()): Boolean {
        val d = now.dayOfWeek
        return !(d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY)
    }

    fun statusText(now: ZonedDateTime = ZonedDateTime.now()): String {
        return if (isMarketOpen(now)) "Market: OPEN" else "Market: CLOSED"
    }
}
