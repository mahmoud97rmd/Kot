package com.tradingapp.metatrader.data.mappers

import com.tradingapp.metatrader.data.local.database.entities.CandleEntity
import com.tradingapp.metatrader.data.remote.dto.OandaCandleDto
import com.tradingapp.metatrader.domain.models.Candle
import com.tradingapp.metatrader.domain.models.Timeframe
import java.time.Instant

fun OandaCandleDto.toDomainOrNull(): Candle? {
    val mid = mid ?: return null
    return Candle(
        time = Instant.parse(time),
        open = mid.o.toDouble(),
        high = mid.h.toDouble(),
        low = mid.l.toDouble(),
        close = mid.c.toDouble(),
        volume = volume ?: 0L
    )
}

fun CandleEntity.toDomain(): Candle = Candle(
    time = Instant.ofEpochSecond(timeEpochSec),
    open = open,
    high = high,
    low = low,
    close = close,
    volume = volume
)

fun Candle.toEntity(instrument: String, timeframe: Timeframe): CandleEntity = CandleEntity(
    instrument = instrument,
    timeframe = timeframe.name,
    timeEpochSec = time.epochSecond,
    open = open,
    high = high,
    low = low,
    close = close,
    volume = volume
)
