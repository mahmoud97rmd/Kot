package com.tradingapp.metatrader.domain.usecases.guards

import java.time.DayOfWeek
import java.time.ZonedDateTime
import javax.inject.Inject

class MarketGuardUseCase @Inject constructor() {

    fun requireMarketOpen(now: ZonedDateTime = ZonedDateTime.now()) {
        val d = now.dayOfWeek
        if (d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY) {
            throw IllegalStateException("Market is closed (weekend).")
        }
    }
}
