package com.tradingapp.metatrader.data.mappers

import com.tradingapp.metatrader.data.local.database.entities.ClosedTradeEntity
import com.tradingapp.metatrader.data.local.database.entities.PositionEntity
import com.tradingapp.metatrader.domain.models.trading.ClosedTrade
import com.tradingapp.metatrader.domain.models.trading.Position
import java.time.Instant

fun PositionEntity.toDomain(): Position =
    Position(
        id = id,
        instrument = instrument,
        side = if (side == "BUY") Position.Side.BUY else Position.Side.SELL,
        entryTime = Instant.ofEpochSecond(entryTimeEpochSec),
        entryPrice = entryPrice,
        lots = lots,
        stopLoss = stopLoss,
        takeProfit = takeProfit,
        comment = comment
    )

fun Position.toEntity(): PositionEntity =
    PositionEntity(
        id = id,
        instrument = instrument,
        side = side.name,
        entryTimeEpochSec = entryTime.epochSecond,
        entryPrice = entryPrice,
        lots = lots,
        stopLoss = stopLoss,
        takeProfit = takeProfit,
        comment = comment
    )

fun ClosedTradeEntity.toDomain(): ClosedTrade =
    ClosedTrade(
        id = id,
        instrument = instrument,
        side = if (side == "BUY") Position.Side.BUY else Position.Side.SELL,
        entryTime = Instant.ofEpochSecond(entryTimeEpochSec),
        exitTime = Instant.ofEpochSecond(exitTimeEpochSec),
        entryPrice = entryPrice,
        exitPrice = exitPrice,
        lots = lots,
        profit = profit,
        comment = comment
    )

fun ClosedTrade.toEntity(): ClosedTradeEntity =
    ClosedTradeEntity(
        id = id,
        instrument = instrument,
        side = side.name,
        entryTimeEpochSec = entryTime.epochSecond,
        exitTimeEpochSec = exitTime.epochSecond,
        entryPrice = entryPrice,
        exitPrice = exitPrice,
        lots = lots,
        profit = profit,
        comment = comment
    )
