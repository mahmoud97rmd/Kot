package com.tradingapp.metatrader.data.local.drawing

import com.tradingapp.metatrader.domain.models.Timeframe
import com.tradingapp.metatrader.domain.models.drawing.DrawingObject
import com.tradingapp.metatrader.domain.models.drawing.DrawingPoint
import com.tradingapp.metatrader.domain.models.drawing.DrawingType
import com.tradingapp.metatrader.domain.repository.DrawingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class DrawingRepositoryImpl(
    private val dao: DrawingDao
) : DrawingRepository {

    override fun observe(instrument: String, timeframe: Timeframe): Flow<List<DrawingObject>> {
        return dao.observeByKey(instrument, timeframe.name).map { entities ->
            entities.mapNotNull { e -> runCatching { fromEntity(e) }.getOrNull() }
        }
    }

    override suspend fun replaceAll(instrument: String, timeframe: Timeframe, objects: List<DrawingObject>) {
        dao.deleteByKey(instrument, timeframe.name)
        val now = System.currentTimeMillis()
        val entities = objects.map { o ->
            DrawingEntity(
                id = o.id,
                instrument = instrument,
                timeframe = timeframe.name,
                type = o.type.name,
                payloadJson = toPayload(o),
                updatedAtMs = now
            )
        }
        dao.insertAll(entities)
    }

    override suspend fun clear(instrument: String, timeframe: Timeframe) {
        dao.deleteByKey(instrument, timeframe.name)
    }

    private fun toPayload(o: DrawingObject): String {
        val obj = JSONObject()
        obj.put("id", o.id)
        obj.put("type", o.type.name)
        obj.put("colorHex", o.colorHex)
        obj.put("lineWidth", o.lineWidth.toDouble())
        obj.put("locked", o.locked)

        val pts = JSONArray()
        for (p in o.points) {
            pts.put(JSONObject().apply {
                put("timeSec", p.timeSec)
                put("price", p.price)
            })
        }
        obj.put("points", pts)
        return obj.toString()
    }

    private fun fromEntity(e: DrawingEntity): DrawingObject {
        val obj = JSONObject(e.payloadJson)
        val id = obj.getString("id")
        val type = DrawingType.valueOf(obj.getString("type"))
        val color = obj.optString("colorHex", "#FFFFFF")
        val width = obj.optDouble("lineWidth", 2.0).toFloat()
        val locked = obj.optBoolean("locked", false)

        val ptsArr = obj.optJSONArray("points") ?: JSONArray()
        val pts = ArrayList<DrawingPoint>(ptsArr.length())
        for (i in 0 until ptsArr.length()) {
            val p = ptsArr.getJSONObject(i)
            pts.add(
                DrawingPoint(
                    timeSec = p.getLong("timeSec"),
                    price = p.getDouble("price")
                )
            )
        }
        return DrawingObject(
            id = id,
            type = type,
            points = pts,
            colorHex = color,
            lineWidth = width,
            locked = locked
        )
    }
}
