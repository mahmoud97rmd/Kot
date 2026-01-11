package com.tradingapp.metatrader.app.features.drawing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingapp.metatrader.domain.models.Timeframe
import com.tradingapp.metatrader.domain.models.drawing.DrawingObject
import com.tradingapp.metatrader.domain.usecases.drawing.ClearDrawingsUseCase
import com.tradingapp.metatrader.domain.usecases.drawing.ObserveDrawingsUseCase
import com.tradingapp.metatrader.domain.usecases.drawing.ReplaceAllDrawingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class DrawingViewModel @Inject constructor(
    private val observe: ObserveDrawingsUseCase,
    private val replaceAll: ReplaceAllDrawingsUseCase,
    private val clearUseCase: ClearDrawingsUseCase
) : ViewModel() {

    private val _current = MutableStateFlow<List<DrawingObject>>(emptyList())
    val current: StateFlow<List<DrawingObject>> = _current

    private var currentKey: Key? = null

    data class Key(val instrument: String, val timeframe: Timeframe)

    fun start(instrument: String, timeframe: Timeframe) {
        val k = Key(instrument, timeframe)
        if (currentKey == k) return
        currentKey = k

        viewModelScope.launch {
            observe(instrument, timeframe).collectLatest {
                _current.value = it
            }
        }
    }

    fun clear() {
        val k = currentKey ?: return
        viewModelScope.launch(Dispatchers.IO) {
            clearUseCase(k.instrument, k.timeframe)
        }
    }

    /**
     * JS sends entire drawings list as JSON string.
     * We store it as replaceAll for this instrument/timeframe.
     */
    fun saveFromJson(json: String) {
        val k = currentKey ?: return
        val list = runCatching { parseJsonList(json) }.getOrElse { emptyList() }
        viewModelScope.launch(Dispatchers.IO) {
            replaceAll(k.instrument, k.timeframe, list)
        }
    }

    fun toJson(list: List<DrawingObject>): String {
        val arr = JSONArray()
        for (o in list) {
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
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun parseJsonList(json: String): List<DrawingObject> {
        val arr = JSONArray(json)
        val out = ArrayList<DrawingObject>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val ptsArr = o.optJSONArray("points") ?: JSONArray()
            val pts = ArrayList<com.tradingapp.metatrader.domain.models.drawing.DrawingPoint>(ptsArr.length())
            for (j in 0 until ptsArr.length()) {
                val p = ptsArr.getJSONObject(j)
                pts.add(
                    com.tradingapp.metatrader.domain.models.drawing.DrawingPoint(
                        timeSec = p.getLong("timeSec"),
                        price = p.getDouble("price")
                    )
                )
            }
            out.add(
                DrawingObject(
                    id = o.getString("id"),
                    type = com.tradingapp.metatrader.domain.models.drawing.DrawingType.valueOf(o.getString("type")),
                    points = pts,
                    colorHex = o.optString("colorHex", "#FFFFFF"),
                    lineWidth = o.optDouble("lineWidth", 2.0).toFloat(),
                    locked = o.optBoolean("locked", false)
                )
            )
        }
        return out
    }
}
