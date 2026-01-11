package com.tradingapp.metatrader.core.utils.time

import com.tradingapp.metatrader.domain.models.Timeframe
import java.time.Instant
import java.time.ZoneOffset

object CandleTime {
    fun floorToTimeframe(t: Instant, tf: Timeframe): Instant {
        val epochSec = t.epochSecond
        val bucket = tf.duration.seconds
        val floored = (epochSec / bucket) * bucket
        return Instant.ofEpochSecond(floored)
    }

    fun nextOpenTime(openTime: Instant, tf: Timeframe): Instant {
        return openTime.plusSeconds(tf.duration.seconds)
    }
}
