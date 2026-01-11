package com.tradingapp.metatrader.app.features.oanda.positions

import com.tradingapp.metatrader.app.core.trading.positions.CloseResult
import com.tradingapp.metatrader.app.core.trading.positions.OpenPositionSummary
import com.tradingapp.metatrader.app.core.trading.positions.PositionService
import com.tradingapp.metatrader.app.features.oanda.net.OandaEndpoints
import com.tradingapp.metatrader.app.features.oanda.settings.OandaSettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OandaPositionService @Inject constructor(
    private val okHttp: OkHttpClient,
    private val settingsStore: OandaSettingsStore
) : PositionService {

    override suspend fun getOpenPositions(): List<OpenPositionSummary> = withContext(Dispatchers.IO) {
        val s = settingsStore.settingsFlow.first()
        if (s.token.isBlank() || s.accountId.isBlank()) return@withContext emptyList()

        // GET /v3/accounts/{accountID}/openPositions :contentReference[oaicite:4]{index=4}
        val base = OandaEndpoints.restBase(s.env)
        val url = "$base/v3/accounts/${s.accountId}/openPositions"

        val req = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${s.token}")
            .get()
            .build()

        val resp = okHttp.newCall(req).execute()
        if (!resp.isSuccessful) return@withContext emptyList()

        val raw = resp.body?.string() ?: return@withContext emptyList()
        val obj = runCatching { JSONObject(raw) }.getOrNull() ?: return@withContext emptyList()

        val positionsArr: JSONArray = obj.optJSONArray("positions") ?: JSONArray()
        val out = ArrayList<OpenPositionSummary>(positionsArr.length())

        for (i in 0 until positionsArr.length()) {
            val p = positionsArr.optJSONObject(i) ?: continue
            val instrument = p.optString("instrument", "")
            if (instrument.isBlank()) continue

            val longObj = p.optJSONObject("long")
            val shortObj = p.optJSONObject("short")

            val longUnits = parseUnits(longObj?.optString("units"))
            val shortUnits = parseUnits(shortObj?.optString("units"))

            out.add(OpenPositionSummary(instrument = instrument, longUnits = longUnits, shortUnits = shortUnits))
        }

        out
    }

    override suspend fun closeInstrumentAll(instrument: String): CloseResult = withContext(Dispatchers.IO) {
        val s = settingsStore.settingsFlow.first()
        if (s.token.isBlank() || s.accountId.isBlank()) {
            return@withContext CloseResult(false, "Missing OANDA token/accountId.")
        }

        // PUT /v3/accounts/{accountID}/positions/{instrument}/close with {"longUnits":"ALL","shortUnits":"ALL"} :contentReference[oaicite:5]{index=5}
        val base = OandaEndpoints.restBase(s.env)
        val url = "$base/v3/accounts/${s.accountId}/positions/$instrument/close"

        val payload = JSONObject().apply {
            put("longUnits", "ALL")
            put("shortUnits", "ALL")
        }.toString()

        val body = payload.toRequestBody("application/json".toMediaType())
        val req = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${s.token}")
            .addHeader("Content-Type", "application/json")
            .put(body)
            .build()

        val resp = okHttp.newCall(req).execute()
        val raw = resp.body?.string()

        if (!resp.isSuccessful) {
            return@withContext CloseResult(false, "Close failed HTTP ${resp.code}", raw)
        }
        CloseResult(true, "Close OK", raw)
    }

    private fun parseUnits(s: String?): Long {
        if (s.isNullOrBlank()) return 0L
        // OANDA returns units as string; may include "-" for short.
        return s.toLongOrNull() ?: 0L
    }
}
