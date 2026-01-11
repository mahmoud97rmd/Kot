package com.tradingapp.metatrader.app.features.sessions.store

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tradingapp.metatrader.app.features.sessions.model.ChartSession
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.chartSessionsDataStore by preferencesDataStore(name = "chart_sessions")

@Singleton
class ChartSessionsStore @Inject constructor(
    @ApplicationContext private val ctx: Context
) {
    private val KEY = stringPreferencesKey("sessions_json")

    val sessionsFlow: Flow<List<ChartSession>> =
        ctx.chartSessionsDataStore.data.map { prefs ->
            parseSessions(prefs[KEY] ?: "[]")
        }

    suspend fun getNow(): List<ChartSession> = sessionsFlow.first()

    suspend fun createSession(symbol: String, timeframe: String, title: String? = null): ChartSession {
        val now = System.currentTimeMillis()
        val s = ChartSession(
            id = UUID.randomUUID().toString(),
            symbol = symbol.trim(),
            timeframe = timeframe.trim().uppercase(),
            title = (title?.trim().takeUnless { it.isNullOrBlank() } ?: "${symbol.trim()} ${timeframe.trim().uppercase()}"),
            createdAtMs = now,
            lastUsedAtMs = now
        )
        updateList { list -> list + s }
        return s
    }

    suspend fun deleteSession(id: String) {
        updateList { list -> list.filterNot { it.id == id } }
    }

    suspend fun touch(id: String) {
        val now = System.currentTimeMillis()
        updateList { list ->
            list.map { if (it.id == id) it.copy(lastUsedAtMs = now) else it }
        }
    }

    suspend fun updateTitle(id: String, newTitle: String) {
        val t = newTitle.trim()
        if (t.isBlank()) return
        updateList { list ->
            list.map { if (it.id == id) it.copy(title = t) else it }
        }
    }

    private suspend fun updateList(transform: (List<ChartSession>) -> List<ChartSession>) {
        ctx.chartSessionsDataStore.edit { prefs ->
            val cur = parseSessions(prefs[KEY] ?: "[]")
            val next = transform(cur)
            prefs[KEY] = toJson(next)
        }
    }

    private fun parseSessions(json: String): List<ChartSession> {
        val arr = runCatching { JSONArray(json) }.getOrNull() ?: JSONArray()
        val out = ArrayList<ChartSession>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            out.add(
                ChartSession(
                    id = o.optString("id"),
                    symbol = o.optString("symbol"),
                    timeframe = o.optString("timeframe"),
                    title = o.optString("title"),
                    createdAtMs = o.optLong("createdAtMs"),
                    lastUsedAtMs = o.optLong("lastUsedAtMs")
                )
            )
        }
        return out
            .filter { it.id.isNotBlank() && it.symbol.isNotBlank() && it.timeframe.isNotBlank() }
            .sortedByDescending { it.lastUsedAtMs }
    }

    private fun toJson(list: List<ChartSession>): String {
        val arr = JSONArray()
        for (s in list) {
            val o = JSONObject()
            o.put("id", s.id)
            o.put("symbol", s.symbol)
            o.put("timeframe", s.timeframe)
            o.put("title", s.title)
            o.put("createdAtMs", s.createdAtMs)
            o.put("lastUsedAtMs", s.lastUsedAtMs)
            arr.put(o)
        }
        return arr.toString()
    }
}
