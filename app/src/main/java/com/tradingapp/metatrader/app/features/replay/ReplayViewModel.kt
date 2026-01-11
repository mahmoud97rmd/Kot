package com.tradingapp.metatrader.app.features.replay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingapp.metatrader.app.state.TradingTickRouter
import com.tradingapp.metatrader.domain.models.Candle
import com.tradingapp.metatrader.domain.models.Timeframe
import com.tradingapp.metatrader.domain.usecases.market.GetHistoricalCandlesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class ReplayViewModel @Inject constructor(
    private val getHistorical: GetHistoricalCandlesUseCase,
    private val tickRouter: TradingTickRouter
) : ViewModel() {

    data class State(
        val enabled: Boolean = false,
        val playing: Boolean = false,
        val speed: Int = 1,
        val instrument: String = "XAU_USD",
        val timeframe: Timeframe = Timeframe.M1,
        val index: Int = 0,
        val total: Int = 0,
        val current: Candle? = null,
        val historyWindow: List<Candle> = emptyList()
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private var job: Job? = null
    private var candles: List<Candle> = emptyList()

    fun setEnabled(enabled: Boolean, instrument: String) {
        if (!enabled) {
            stop()
            tickRouter.setReplayMode(false)
            _state.value = _state.value.copy(
                enabled = false, playing = false, instrument = instrument, index = 0, total = 0,
                current = null, historyWindow = emptyList()
            )
            return
        }

        viewModelScope.launch {
            // enable router replay mode
            tickRouter.setReplayMode(true)

            candles = getHistorical(instrument, _state.value.timeframe, 500)
            val cur = candles.firstOrNull()
            _state.value = _state.value.copy(
                enabled = true,
                playing = false,
                instrument = instrument,
                index = 0,
                total = candles.size,
                current = cur,
                historyWindow = candles.take(200)
            )

            // Push initial tick so trading engine starts aligned (if needed)
            cur?.let { pushReplayTick(it) }
        }
    }

    fun setSpeed(x: Int) {
        val s = if (x <= 0) 1 else x
        _state.value = _state.value.copy(speed = s)
    }

    fun play() {
        if (!_state.value.enabled) return
        if (_state.value.playing) return
        _state.value = _state.value.copy(playing = true)
        job?.cancel()
        job = viewModelScope.launch {
            while (_state.value.playing && _state.value.enabled) {
                stepForward()
                val delayMs = when (_state.value.speed) {
                    1 -> 700L
                    2 -> 350L
                    5 -> 140L
                    10 -> 70L
                    else -> (700L / _state.value.speed.toLong()).coerceAtLeast(20L)
                }
                delay(delayMs)
            }
        }
    }

    fun pause() {
        _state.value = _state.value.copy(playing = false)
        job?.cancel()
        job = null
    }

    fun stop() {
        pause()
        candles = emptyList()
    }

    fun stepForward() {
        if (!_state.value.enabled) return
        if (candles.isEmpty()) return

        val nextIndex = (_state.value.index + 1).coerceAtMost(candles.lastIndex)
        val c = candles[nextIndex]

        val windowStart = (nextIndex - 199).coerceAtLeast(0)
        val window = candles.subList(windowStart, nextIndex + 1)

        _state.value = _state.value.copy(
            index = nextIndex,
            total = candles.size,
            current = c,
            historyWindow = window
        )

        pushReplayTick(c)

        if (nextIndex == candles.lastIndex) {
            _state.value = _state.value.copy(playing = false)
            job?.cancel()
            job = null
        }
    }

    private fun pushReplayTick(c: Candle) {
        // Simple synthetic bid/ask from close.
        // Spread is minimal and deterministic (educational simulation).
        val mid = c.close
        val spread = kotlin.math.max(0.01, mid * 0.00002) // 2 bps or 0.01 min
        val bid = mid - spread / 2.0
        val ask = mid + spread / 2.0

        tickRouter.onReplayTick(
            time = c.time,
            bid = bid,
            ask = ask
        )
    }
}
