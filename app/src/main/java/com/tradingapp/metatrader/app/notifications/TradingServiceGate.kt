package com.tradingapp.metatrader.app.notifications

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TradingServiceGate {
    private val _streamingActive = MutableStateFlow(false)
    val streamingActive: StateFlow<Boolean> = _streamingActive.asStateFlow()

    private val _autoTradingActive = MutableStateFlow(false)
    val autoTradingActive: StateFlow<Boolean> = _autoTradingActive.asStateFlow()

    private val _shouldRunInBackground = MutableStateFlow(false)
    val shouldRunInBackground: StateFlow<Boolean> = _shouldRunInBackground.asStateFlow()

    fun setStreamingActive(active: Boolean) {
        _streamingActive.value = active
        recalc()
    }

    fun setAutoTradingActive(active: Boolean) {
        _autoTradingActive.value = active
        recalc()
    }

    private fun recalc() {
        _shouldRunInBackground.value = _streamingActive.value || _autoTradingActive.value
    }
}
