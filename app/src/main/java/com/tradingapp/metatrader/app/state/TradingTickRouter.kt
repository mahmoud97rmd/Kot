package com.tradingapp.metatrader.app.state

import com.tradingapp.metatrader.domain.repository.TradingEngineInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant

class TradingTickRouter(
    private val tradingInput: TradingEngineInput
) {
    private val _replayMode = MutableStateFlow(false)
    val replayMode: StateFlow<Boolean> = _replayMode.asStateFlow()

    fun setReplayMode(enabled: Boolean) {
        _replayMode.value = enabled
    }

    fun onLiveTick(time: Instant, bid: Double, ask: Double) {
        if (_replayMode.value) return
        tradingInput.onTick(time, bid, ask)
    }

    fun onReplayTick(time: Instant, bid: Double, ask: Double) {
        if (!_replayMode.value) return
        tradingInput.onTick(time, bid, ask)
    }
}
