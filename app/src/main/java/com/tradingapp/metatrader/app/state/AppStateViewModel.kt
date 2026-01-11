package com.tradingapp.metatrader.app.state

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AppStateViewModel : ViewModel() {

    private val _selectedInstrument = MutableStateFlow("XAU_USD")
    val selectedInstrument: StateFlow<String> = _selectedInstrument.asStateFlow()

    // Simplified price map (mid price). If later you store bid/ask separately, you can extend.
    private val _prices = MutableStateFlow<Map<String, Double>>(emptyMap())
    val prices: StateFlow<Map<String, Double>> = _prices.asStateFlow()

    private val _replayMode = MutableStateFlow(false)
    val replayMode: StateFlow<Boolean> = _replayMode.asStateFlow()

    private val _autoTradingEnabled = MutableStateFlow(false)
    val autoTradingEnabled: StateFlow<Boolean> = _autoTradingEnabled.asStateFlow()

    private val _oneClickEnabled = MutableStateFlow(false)
    val oneClickEnabled: StateFlow<Boolean> = _oneClickEnabled.asStateFlow()

    private val _quickLots = MutableStateFlow(1.0)
    val quickLots: StateFlow<Double> = _quickLots.asStateFlow()

    fun setInstrument(instrument: String) {
        _selectedInstrument.value = instrument
    }

    fun updatePrice(instrument: String, mid: Double) {
        _prices.update { old ->
            val m = old.toMutableMap()
            m[instrument] = mid
            m
        }
    }

    fun setReplayMode(enabled: Boolean) {
        _replayMode.value = enabled
    }

    fun setAutoTradingEnabled(enabled: Boolean) {
        _autoTradingEnabled.value = enabled
    }

    fun setOneClickEnabled(enabled: Boolean) {
        _oneClickEnabled.value = enabled
    }

    fun setQuickLots(lots: Double) {
        val safe = if (lots <= 0.0) 0.01 else lots
        _quickLots.value = safe
    }

    fun incLots(step: Double = 0.01) {
        setQuickLots(_quickLots.value + step)
    }

    fun decLots(step: Double = 0.01) {
        setQuickLots((_quickLots.value - step).coerceAtLeast(0.01))
    }
}
