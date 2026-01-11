package com.tradingapp.metatrader.app.data.remote.oanda.stream

import com.tradingapp.metatrader.app.core.market.MarketTick
import com.tradingapp.metatrader.app.core.market.feed.MarketFeed
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean

class OandaMarketFeed(
    private val http: OkHttpClient,
    private val streamBaseUrl: String,
    private val accountId: String
) : MarketFeed {

    override fun ticks(instruments: List<String>): Flow<MarketTick> = callbackFlow {
        if (instruments.isEmpty()) {
            close(IllegalArgumentException("instruments empty"))
            return@callbackFlow
        }

        val ins = instruments.joinToString(separator = ",")
        val url = buildString {
            append(streamBaseUrl)
            append("/v3/accounts/")
            append(accountId)
            append("/pricing/stream?instruments=")
            append(ins)
        }

        val req = Request.Builder()
            .url(url)
            .get()
            .build()

        val closed = AtomicBoolean(false)

        val call = http.newCall(req)

        val thread = Thread {
            try {
                call.execute().use { resp ->
                    if (!resp.isSuccessful) {
                        val body = resp.body?.string().orEmpty()
                        if (!closed.get()) close(IllegalStateException("OANDA stream failed HTTP ${resp.code}: ${body.take(400)}"))
                        return@use
                    }

                    val source = resp.body?.source()
                    if (source == null) {
                        if (!closed.get()) close(IllegalStateException("OANDA stream empty body"))
                        return@use
                    }

                    while (!source.exhausted() && !closed.get()) {
                        val line = source.readUtf8Line()
                        if (line.isNullOrBlank()) continue

                        val json = runCatching { JSONObject(line) }.getOrNull() ?: continue
                        val type = json.optString("type")

                        if (type == "HEARTBEAT") {
                            continue
                        }

                        if (type == "PRICE") {
                            val instrument = json.optString("instrument")
                            val timeIso = json.optString("time")
                            val tMs = isoToEpochMs(timeIso)

                            val bids = json.optJSONArray("bids")
                            val asks = json.optJSONArray("asks")
                            val bid = bids?.optJSONObject(0)?.optString("price")?.toDoubleOrNull()
                            val ask = asks?.optJSONObject(0)?.optString("price")?.toDoubleOrNull()
                            if (bid == null || ask == null) continue

                            trySend(
                                MarketTick(
                                    instrument = instrument,
                                    timeEpochMs = tMs,
                                    bid = bid,
                                    ask = ask
                                )
                            )
                        }
                    }
                }
            } catch (t: Throwable) {
                if (!closed.get()) close(t)
            }
        }

        thread.name = "OandaMarketFeed-Stream"
        thread.start()

        awaitClose {
            closed.set(true)
            try { call.cancel() } catch (_: Throwable) {}
        }
    }

    private fun isoToEpochMs(iso: String): Long {
        return runCatching { Instant.parse(iso).toEpochMilli() }
            .getOrElse {
                val trimmed = iso.substringBefore(".") + "Z"
                Instant.parse(trimmed).toEpochMilli()
            }
    }
}
