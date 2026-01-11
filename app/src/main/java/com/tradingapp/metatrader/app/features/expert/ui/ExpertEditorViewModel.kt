package com.tradingapp.metatrader.app.features.expert.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingapp.metatrader.app.features.expert.data.ExpertScriptRepository
import com.tradingapp.metatrader.domain.models.expert.ExpertScript
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpertEditorViewModel @Inject constructor(
    private val repo: ExpertScriptRepository
) : ViewModel() {

    data class UiState(
        val loading: Boolean = true,
        val script: ExpertScript? = null,
        val error: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun load(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = UiState(loading = true)
            val s = runCatching { repo.getById(id) }.getOrNull()
            if (s == null) {
                _state.value = UiState(loading = false, script = null, error = "Script not found")
            } else {
                _state.value = UiState(loading = false, script = s, error = null)
            }
        }
    }

    fun save(id: String, name: String, code: String, isEnabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val old = repo.getById(id) ?: return@launch
            val updated = old.copy(
                name = name,
                code = code,
                updatedAtMs = System.currentTimeMillis(),
                isEnabled = isEnabled
            )
            repo.upsert(updated)
        }
    }

    fun enableExclusive(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.enableExclusive(id)
            // refresh state
            val s = repo.getById(id)
            _state.value = _state.value.copy(script = s)
        }
    }
}
