package com.tradingapp.metatrader.app.data.remote.oanda.rest

import com.tradingapp.metatrader.app.core.market.MarketCandle
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.time.Instant

class OandaRestClient(
    private val http: OkHttpClient
) {

    /**
     * GET /v3/instruments/{instrument}/candles?granularity=M1&count=500&price=M
     */
    fun getCandles(
        envBaseUrl: String,
        instrument: String,
        granularity: String,
        count: Int = 500
    ): List<MarketCandle> {
        val url = buildString {
            append(envBaseUrl)
            append("/v3/instruments/")
            append(instrument)
            append("/candles")
            append("?granularity=").append(granularity)
            append("&count=").append(count.coerceIn(1, 5000))
            append("&price=M")
        }

        return fetchCandles(url)
    }

    /**
     * Optional (for future gap filling by time window):
     * GET /v3/instruments/{instrument}/candles?granularity=M1&from=...&price=M
     * OANDA expects "from" as RFC3339 or unix timestamp. We'll pass RFC3339.
     */
    fun getCandlesSince(
        envBaseUrl: String,
        instrument: String,
        granularity: String,
        fromEpochSec: Long,
        count: Int = 500
    ): List<MarketCandle> {
        val fromIso = Instant.ofEpochSecond(fromEpochSec).toString()
        val url = buildString {
            append(envBaseUrl)
            append("/v3/instruments/")
            append(instrument)
            append("/candles")
            append("?granularity=").append(granularity)
            append("&from=").append(fromIso)
            append("&count=").append(count.coerceIn(1, 5000))
            append("&price=M")
        }
        return fetchCandles(url)
    }

    private fun fetchCandles(url: String): List<MarketCandle> {
        val req = Request.Builder().url(url).get().build()
        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                val body = resp.body?.string().orEmpty()
                throw IllegalStateException("OANDA candles failed: HTTP ${resp.code} ${resp.message} body=${body.take(500)}")
            }

            val body = resp.body?.string().orEmpty()
            val json = JSONObject(body)
            val arr = json.optJSONArray("candles") ?: return emptyList()

            val out = ArrayList<MarketCandle>(arr.length())
            for (i in 0 until arr.length()) {
                val c = arr.getJSONObject(i)
                if (!c.optBoolean("complete", true)) continue

                val timeIso = c.optString("time")
                val timeSec = isoToEpochSec(timeIso)
                val mid = c.optJSONObject("mid") ?: continue

                val o = mid.optString("o").toDoubleOrNull() ?: continue
                val h = mid.optString("h").toDoubleOrNull() ?: continue
                val l = mid.optString("l").toDoubleOrNull() ?: continue
                val cl = mid.optString("c").toDoubleOrNull() ?: continue
                val vol = c.optLong("volume", 0L)

                out.add(
                    MarketCandle(
                        timeSec = timeSec,
                        open = o,
                        high = h,
                        low = l,
                        close = cl,
                        volume = vol
                    )
                )
            }
            return out
        }
    }

    private fun isoToEpochSec(iso: String): Long {
        return runCatching { Instant.parse(iso).epochSecond }
            .getOrElse {
                val trimmed = iso.substringBefore(".") + "Z"
                Instant.parse(trimmed).epochSecond
            }
    }
}
