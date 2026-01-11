package com.tradingapp.metatrader.data.mappers

import com.tradingapp.metatrader.data.local.database.entities.PendingOrderEntity
import com.tradingapp.metatrader.domain.models.trading.PendingOrder
import java.time.Instant

fun PendingOrderEntity.toDomain(): PendingOrder =
    PendingOrder(
        id = id,
        instrument = instrument,
        type = PendingOrder.Type.valueOf(type),
        createdAt = Instant.ofEpochSecond(createdAtEpochSec),
        targetPrice = targetPrice,
        lots = lots,
        stopLoss = stopLoss,
        takeProfit = takeProfit,
        comment = comment
    )

fun PendingOrder.toEntity(): PendingOrderEntity =
    PendingOrderEntity(
        id = id,
        instrument = instrument,
        type = type.name,
        createdAtEpochSec = createdAt.epochSecond,
        targetPrice = targetPrice,
        lots = lots,
        stopLoss = stopLoss,
        takeProfit = takeProfit,
        comment = comment
    )
