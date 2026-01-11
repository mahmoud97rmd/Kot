package com.tradingapp.metatrader.app.features.oanda.streaming

import com.tradingapp.metatrader.app.core.market.MarketTick
import com.tradingapp.metatrader.app.core.market.feed.MarketFeed
import com.tradingapp.metatrader.app.features.oanda.net.OandaEndpoints
import com.tradingapp.metatrader.app.features.oanda.settings.OandaSettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OandaPricingStreamFeed @Inject constructor(
    private val okHttp: OkHttpClient,
    private val settingsStore: OandaSettingsStore
) : MarketFeed {

    override fun ticks(symbols: List<String>): Flow<MarketTick> = channelFlow {
        val s = settingsStore.settingsFlow.first()

        if (s.token.isBlank() || s.accountId.isBlank()) {
            // compile-safe behavior: emit nothing, but keep a clear log.
            println("OandaPricingStreamFeed: Missing token/accountId. Configure OANDA settings first.")
            close()
            return@channelFlow
        }

        val instruments = symbols.joinToString(",")
        val base = OandaEndpoints.pricingStreamBase(s.env)
        val url = "$base/v3/accounts/${s.accountId}/pricing/stream?instruments=$instruments"

        val req = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${s.token}")
            .get()
            .build()

        // Run blocking IO reading in IO dispatcher
        launch(Dispatchers.IO) {
            val call = okHttp.newCall(req)
            val resp = call.execute()
            if (!resp.isSuccessful) {
                println("OandaPricingStreamFeed: HTTP ${resp.code} ${resp.message}")
                close()
                return@launch
            }

            val body = resp.body ?: run {
                println("OandaPricingStreamFeed: empty body")
                close()
                return@launch
            }

            BufferedReader(InputStreamReader(body.byteStream())).use { reader ->
                while (!isClosedForSend) {
                    val line = reader.readLine() ?: break
                    if (line.isBlank()) continue

                    // OANDA stream sends different message types: PRICE, HEARTBEAT, etc.
                    // We only parse PRICE messages.
                    val obj = runCatching { JSONObject(line) }.getOrNull() ?: continue
                    val type = obj.optString("type", "")
                    if (type != "PRICE") continue

                    val instrument = obj.optString("instrument", "")
                    if (instrument.isBlank()) continue

                    val timeStr = obj.optString("time", "")
                    val timeMs = parseIsoTimeToEpochMs(timeStr)

                    val bids = obj.optJSONArray("bids")
                    val asks = obj.optJSONArray("asks")
                    if (bids == null || asks == null || bids.length() == 0 || asks.length() == 0) continue

                    val bid = bids.optJSONObject(0)?.optString("price")?.toDoubleOrNull() ?: continue
                    val ask = asks.optJSONObject(0)?.optString("price")?.toDoubleOrNull() ?: continue

                    trySend(MarketTick(symbol = instrument, timeEpochMs = timeMs, bid = bid, ask = ask))
                }
            }

            close()
        }
    }

    private fun parseIsoTimeToEpochMs(iso: String): Long {
        // Very small safe parser (avoids adding heavy libs).
        // If parsing fails, fallback to current time.
        return runCatching {
            // Example: 2020-01-01T00:00:00.000000000Z
            val clean = iso.replace("Z", "")
            val main = clean.split(".")[0]
            val parts = main.split("T")
            val date = parts[0].split("-").map { it.toInt() }
            val time = parts[1].split(":").map { it.toInt() }
            val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
            cal.set(date[0], date[1] - 1, date[2], time[0], time[1], time[2])
            cal.set(java.util.Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.getOrElse { System.currentTimeMillis() }
    }
}
