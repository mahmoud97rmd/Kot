package com.tradingapp.metatrader.app.features.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingapp.metatrader.app.core.autotrading.AutoTradingStore
import com.tradingapp.metatrader.app.features.expert.data.ExpertAttachmentRepository
import com.tradingapp.metatrader.app.features.expert.data.ExpertScriptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChartViewModel @Inject constructor(
    private val attachments: ExpertAttachmentRepository,
    private val scripts: ExpertScriptRepository,
    private val autoTrading: AutoTradingStore
) : ViewModel() {

    data class UiState(
        val symbol: String = "XAU_USD",
        val timeframe: String = "M1",
        val attachedScriptId: String? = null,
        val attachedScriptName: String? = null,
        val autoTradingOn: Boolean = false
    )

    private val symbolFlow = MutableStateFlow("XAU_USD")
    private val timeframeFlow = MutableStateFlow("M1")

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    init {
        viewModelScope.launch {
            combine(
                symbolFlow,
                timeframeFlow,
                attachments.observeAll(),
                autoTrading.enabledFlow
            ) { sym, tf, list, enabled ->
                val att = list.firstOrNull { it.symbol == sym && it.timeframe == tf && it.isActive }
                val scriptName = att?.scriptId?.let { scripts.getById(it)?.name }
                UiState(
                    symbol = sym,
                    timeframe = tf,
                    attachedScriptId = att?.scriptId,
                    attachedScriptName = scriptName,
                    autoTradingOn = enabled
                )
            }.collectLatest { st ->
                _state.value = st
            }
        }
    }

    fun setSymbol(symbol: String) {
        val s = symbol.trim()
        if (s.isNotBlank()) symbolFlow.value = s
    }

    fun setTimeframe(tf: String) {
        val t = tf.trim().uppercase()
        if (t.isNotBlank()) timeframeFlow.value = t
    }
}
