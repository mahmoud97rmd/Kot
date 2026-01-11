package com.tradingapp.metatrader.app.features.backtest.data.oanda

import com.tradingapp.metatrader.app.features.backtest.data.oanda.dto.OandaCandleDto
import com.tradingapp.metatrader.app.features.backtest.data.oanda.gaps.TimeGap
import com.tradingapp.metatrader.app.features.backtest.data.oanda.utils.OandaGranularity
import com.tradingapp.metatrader.app.features.backtest.data.room.BacktestCandleRepository
import com.tradingapp.metatrader.domain.models.backtest.BacktestCandle
import kotlinx.coroutines.delay
import okhttp3.internal.http2.StreamResetException
import retrofit2.HttpException
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class OandaHistoryDownloader @Inject constructor(
    private val api: OandaApiService,
    private val repo: BacktestCandleRepository
) {

    /**
     * تنزيل وتخزين candles لتغطية from/to أو latest إذا null.
     *
     * OANDA candles endpoint supports from/to/count/granularity/price :contentReference[oaicite:6]{index=6}
     * Rate limiting: 120 req/s, 429 on excess :contentReference[oaicite:7]{index=7}
     */
    suspend fun downloadAndStore(
        instrument: String,
        granularity: String,
        fromSec: Long?,
        toSec: Long?,
        maxCandles: Int
    ): Boolean {
        return try {
            val fromIso = fromSec?.let { toIso8601(it) }
            val toIso = toSec?.let { toIso8601(it) }

            val resp = api.getCandles(
                instrument = instrument,
                granularity = granularity,
                price = "M",
                fromIso = fromIso,
                toIso = toIso,
                count = if (fromIso == null && toIso == null) maxCandles else null
            )

            val candles = resp.candles
                .asSequence()
                .filter { it.complete }
                .mapNotNull { it.toDomainOrNull() }
                .toList()

            if (candles.size < 50) return false
            repo.upsertAll(instrument, granularity, candles)
            delay(50)
            true
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * تنزيل gaps فقط (paging windows).
     * windowCandles يحدد حجم النافذة (كم شمعة تقريباً لكل طلب).
     */
    suspend fun downloadGapsAndStore(
        instrument: String,
        granularity: String,
        gaps: List<TimeGap>,
        windowCandles: Int = 2000,
        onProgress: ((String) -> Unit)? = null
    ): Boolean {
        if (gaps.isEmpty()) return true
        val step = OandaGranularity.seconds(granularity).coerceAtLeast(1L)

        var okAny = false
        for ((gi, gap) in gaps.withIndex()) {
            onProgress?.invoke("Gap ${gi + 1}/${gaps.size}: [${gap.fromSec}..${gap.toSec}]")

            var curFrom = gap.fromSec
            while (curFrom <= gap.toSec) {
                val curTo = min(gap.toSec, curFrom + step * windowCandles)

                val success = fetchWindowWithRetry(
                    instrument = instrument,
                    granularity = granularity,
                    fromSec = curFrom,
                    toSec = curTo,
                    onProgress = onProgress
                )

                if (success) okAny = true

                // advance to next window (avoid overlap)
                curFrom = curTo + step
            }
        }
        return okAny
    }

    private suspend fun fetchWindowWithRetry(
        instrument: String,
        granularity: String,
        fromSec: Long,
        toSec: Long,
        onProgress: ((String) -> Unit)?
    ): Boolean {
        val fromIso = toIso8601(fromSec)
        val toIso = toIso8601(toSec)

        var attempt = 0
        var backoffMs = 250L

        while (attempt < 6) {
            attempt++
            try {
                onProgress?.invoke("Downloading $instrument $granularity [$fromSec..$toSec] (try $attempt)")

                val resp = api.getCandles(
                    instrument = instrument,
                    granularity = granularity,
                    price = "M",
                    fromIso = fromIso,
                    toIso = toIso,
                    count = null
                )

                val candles = resp.candles
                    .asSequence()
                    .filter { it.complete }
                    .mapNotNull { it.toDomainOrNull() }
                    .toList()

                if (candles.isNotEmpty()) {
                    repo.upsertAll(instrument, granularity, candles)
                    // small delay to keep below recommended established connection throughput :contentReference[oaicite:8]{index=8}
                    delay(30)
                    return true
                }
                return false
            } catch (e: HttpException) {
                // 429 rate limit => backoff :contentReference[oaicite:9]{index=9}
                if (e.code() == 429) {
                    onProgress?.invoke("Rate limit (429) backing off ${backoffMs}ms")
                    delay(backoffMs)
                    backoffMs = (backoffMs * 2).coerceAtMost(4000L)
                    continue
                }
                onProgress?.invoke("HTTP ${e.code()} failed")
                return false
            } catch (_: StreamResetException) {
                // network hiccup, backoff and retry
                delay(backoffMs)
                backoffMs = (backoffMs * 2).coerceAtMost(4000L)
            } catch (_: Throwable) {
                delay(backoffMs)
                backoffMs = (backoffMs * 2).coerceAtMost(4000L)
            }
        }
        return false
    }

    private fun toIso8601(sec: Long): String =
        DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(sec))

    private fun OandaCandleDto.toDomainOrNull(): BacktestCandle? {
        val m = this.mid ?: return null
        val tSec = runCatching { Instant.parse(this.time).epochSecond }.getOrNull() ?: return null

        val o = m.o.toDoubleOrNull() ?: return null
        val h = m.h.toDoubleOrNull() ?: return null
        val l = m.l.toDoubleOrNull() ?: return null
        val c = m.c.toDoubleOrNull() ?: return null

        return BacktestCandle(timeSec = tSec, open = o, high = h, low = l, close = c)
    }
}
