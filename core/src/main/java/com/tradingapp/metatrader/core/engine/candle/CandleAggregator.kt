package com.tradingapp.metatrader.core.engine.candle

import com.tradingapp.metatrader.core.utils.time.CandleTime
import com.tradingapp.metatrader.domain.models.Candle
import com.tradingapp.metatrader.domain.models.Tick
import com.tradingapp.metatrader.domain.models.Timeframe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.time.Instant
import kotlin.math.max
import kotlin.math.min

/**
 * يحول الـ Ticks إلى شموع OHLC حسب Timeframe.
 * يطلق:
 *  - candleUpdates: تحديثات للشمعة الحالية (غير مغلقة)
 *  - candleClosed: شمعة مغلقة عند اكتمال الفترة
 */
class CandleAggregator(
    private val timeframe: Timeframe
) {
    private var currentOpenTime: Instant? = null
    private var current: Candle? = null

    private val _candleUpdates = MutableSharedFlow<Candle>(replay = 1, extraBufferCapacity = 64)
    val candleUpdates: SharedFlow<Candle> = _candleUpdates

    private val _candleClosed = MutableSharedFlow<Candle>(replay = 0, extraBufferCapacity = 64)
    val candleClosed: SharedFlow<Candle> = _candleClosed

    fun onTick(tick: Tick) {
        val tickTime = tick.time
        val openTime = CandleTime.floorToTimeframe(tickTime, timeframe)

        if (current == null || currentOpenTime == null) {
            // بداية أول شمعة
            currentOpenTime = openTime
            current = Candle(
                time = openTime,
                open = tick.mid,
                high = tick.mid,
                low = tick.mid,
                close = tick.mid,
                volume = 0L
            )
            _candleUpdates.tryEmit(current!!)
            return
        }

        val curOpen = currentOpenTime!!
        val nextOpen = CandleTime.nextOpenTime(curOpen, timeframe)

        if (!tickTime.isBefore(nextOpen)) {
            // أغلق الشمعة الحالية (قد نكون تجاوزنا أكثر من شمعة إن كانت التكات متقطعة)
            val closed = current!!
            _candleClosed.tryEmit(closed)

            // افتح شمعة جديدة على openTime الحالي
            currentOpenTime = openTime
            current = Candle(
                time = openTime,
                open = tick.mid,
                high = tick.mid,
                low = tick.mid,
                close = tick.mid,
                volume = 0L
            )
            _candleUpdates.tryEmit(current!!)
            return
        }

        // تحديث داخل نفس الشمعة
        val c = current!!
        val price = tick.mid
        val updated = c.copy(
            high = max(c.high, price),
            low = min(c.low, price),
            close = price,
            volume = c.volume + 1
        )
        current = updated
        _candleUpdates.tryEmit(updated)
    }
}
