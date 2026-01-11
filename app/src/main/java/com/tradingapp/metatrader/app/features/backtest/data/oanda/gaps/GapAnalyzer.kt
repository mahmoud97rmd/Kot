package com.tradingapp.metatrader.app.features.backtest.data.oanda.gaps

import com.tradingapp.metatrader.app.features.backtest.data.oanda.utils.OandaGranularity
import com.tradingapp.metatrader.app.features.backtest.data.room.BacktestCandleRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GapAnalyzer @Inject constructor(
    private val repo: BacktestCandleRepository
) {

    /**
     * يرجع قائمة فجوات يجب تنزيلها من OANDA لتغطية [fromSec..toSec].
     * strategy:
     * - إذا لا يوجد أي candles -> gap واحدة (كامل المدى)
     * - نحسب missing steps داخل range من خلال times القائمة
     */
    suspend fun findGaps(
        instrument: String,
        granularity: String,
        fromSec: Long,
        toSec: Long
    ): List<TimeGap> {
        if (fromSec > toSec) return emptyList()

        val count = repo.countRange(instrument, granularity, fromSec, toSec)
        if (count <= 0) return listOf(TimeGap(fromSec, toSec))

        val step = OandaGranularity.seconds(granularity).coerceAtLeast(1L)
        val times = repo.getTimesAsc(instrument, granularity, fromSec, toSec)
        if (times.isEmpty()) return listOf(TimeGap(fromSec, toSec))

        val gaps = ArrayList<TimeGap>()

        // start gap
        val first = times.first()
        if (first > fromSec) gaps.add(TimeGap(fromSec, first - step))

        // internal gaps
        var prev = first
        for (i in 1 until times.size) {
            val cur = times[i]
            val expected = prev + step
            if (cur > expected) {
                gaps.add(TimeGap(expected, cur - step))
            }
            prev = cur
        }

        // end gap
        val last = times.last()
        if (last < toSec) gaps.add(TimeGap(last + step, toSec))

        // sanitize (from<=to)
        return gaps.filter { it.fromSec <= it.toSec }
    }
}
