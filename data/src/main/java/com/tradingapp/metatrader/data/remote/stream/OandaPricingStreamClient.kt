package com.tradingapp.metatrader.data.remote.stream

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.tradingapp.metatrader.domain.models.Tick
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant

class OandaPricingStreamClient(
    private val accountId: String,
    private val tokenProvider: () -> String,
    private val isPractice: Boolean,
    private val moshi: Moshi
) {
    private val client = HttpClient(OkHttp) {
        install(HttpTimeout) {
            requestTimeoutMillis = 0
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 0
        }
    }

    fun streamTicks(instrumentsCsv: String): Flow<Tick> = flow {
        val host = if (isPractice) "stream-fxpractice.oanda.com" else "stream-fxtrade.oanda.com"

        val response = client.get {
            url {
                protocol = URLProtocol.HTTPS
                this.host = host
                path("v3", "accounts", accountId, "pricing", "stream")
                parameters.append("instruments", instrumentsCsv)
            }
            headers.append("Authorization", "Bearer ${tokenProvider()}")
        }

        val channel = response.bodyAsChannel()
        while (!channel.isClosedForRead) {
            val line = channel.readUTF8Line() ?: continue
            parseTickOrNull(line)?.let { emit(it) }
        }
    }

    private fun parseTickOrNull(jsonLine: String): Tick? {
        val mapType = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
        val adapter = moshi.adapter<Map<String, Any>>(mapType)

        val obj = runCatching { adapter.fromJson(jsonLine) }.getOrNull() ?: return null
        val type = obj["type"] as? String ?: return null
        if (type != "PRICE") return null

        val instrument = obj["instrument"] as? String ?: return null
        val timeStr = obj["time"] as? String ?: return null

        val bids = obj["bids"] as? List<Map<String, Any>> ?: return null
        val asks = obj["asks"] as? List<Map<String, Any>> ?: return null

        val bidStr = bids.firstOrNull()?.get("price")?.toString() ?: return null
        val askStr = asks.firstOrNull()?.get("price")?.toString() ?: return null

        return Tick(
            instrument = instrument,
            time = Instant.parse(timeStr),
            bid = bidStr.toDouble(),
            ask = askStr.toDouble()
        )
    }
}
