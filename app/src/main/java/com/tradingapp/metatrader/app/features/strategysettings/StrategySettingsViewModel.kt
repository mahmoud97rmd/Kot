package com.tradingapp.metatrader.app.features.strategysettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingapp.metatrader.domain.models.strategy.StrategySettings
import com.tradingapp.metatrader.domain.usecases.strategy.ObserveStrategySettingsUseCase
import com.tradingapp.metatrader.domain.usecases.strategy.UpdateStrategySettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StrategySettingsViewModel @Inject constructor(
    observe: ObserveStrategySettingsUseCase,
    private val update: UpdateStrategySettingsUseCase
) : ViewModel() {

    val settings: StateFlow<StrategySettings> =
        observe().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StrategySettings())

    private val _status = MutableStateFlow("--")
    val status: StateFlow<String> = _status

    fun save(newSettings: StrategySettings) {
        viewModelScope.launch {
            _status.value = "Saving..."
            runCatching {
                update.set(newSettings)
            }.onSuccess {
                _status.value = "Saved"
            }.onFailure { e ->
                _status.value = "Error: ${e.message}"
            }
        }
    }
}
