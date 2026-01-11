package com.tradingapp.metatrader.app.data.mappers

import com.tradingapp.metatrader.data.local.drawing.DrawingEntity
import com.tradingapp.metatrader.app.features.drawing.model.Anchor
import com.tradingapp.metatrader.app.features.drawing.model.DrawingObject
import com.tradingapp.metatrader.app.features.drawing.model.HorizontalLine
import com.tradingapp.metatrader.app.features.drawing.model.TrendLine

object DrawingMapper {

    fun toDomain(e: DrawingEntity): DrawingObject? {
        return when (e.type) {
            "TREND" -> {
                val aT = e.aTimeSec ?: return null
                val aP = e.aPrice ?: return null
                val bT = e.bTimeSec ?: return null
                val bP = e.bPrice ?: return null
                TrendLine(
                    id = e.id,
                    symbol = e.symbol,
                    timeframe = e.timeframe,
                    a = Anchor(aT, aP),
                    b = Anchor(bT, bP)
                )
            }
            "HLINE" -> {
                val p = e.price ?: return null
                HorizontalLine(
                    id = e.id,
                    symbol = e.symbol,
                    timeframe = e.timeframe,
                    price = p
                )
            }
            else -> null
        }
    }

    fun toEntity(obj: DrawingObject, nowMs: Long): DrawingEntity {
        return when (obj) {
            is TrendLine -> DrawingEntity(
                id = obj.id,
                symbol = obj.symbol,
                timeframe = obj.timeframe,
                type = "TREND",
                aTimeSec = obj.a.timeSec,
                aPrice = obj.a.price,
                bTimeSec = obj.b.timeSec,
                bPrice = obj.b.price,
                price = null,
                createdAtMs = nowMs,
                updatedAtMs = nowMs
            )
            is HorizontalLine -> DrawingEntity(
                id = obj.id,
                symbol = obj.symbol,
                timeframe = obj.timeframe,
                type = "HLINE",
                aTimeSec = null,
                aPrice = null,
                bTimeSec = null,
                bPrice = null,
                price = obj.price,
                createdAtMs = nowMs,
                updatedAtMs = nowMs
            )
        }
    }
}
