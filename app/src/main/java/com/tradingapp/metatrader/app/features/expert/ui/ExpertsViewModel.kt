package com.tradingapp.metatrader.app.features.expert.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingapp.metatrader.app.features.expert.data.ExpertAttachmentRepository
import com.tradingapp.metatrader.app.features.expert.data.ExpertScriptRepository
import com.tradingapp.metatrader.app.features.expert.templates.DefaultExpertTemplates
import com.tradingapp.metatrader.domain.models.expert.ExpertScript
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpertsViewModel @Inject constructor(
    private val scriptsRepo: ExpertScriptRepository,
    private val attachRepo: ExpertAttachmentRepository
) : ViewModel() {

    val scripts: StateFlow<List<ExpertScript>> =
        scriptsRepo.observeAll()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun createNew(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            scriptsRepo.createScript(name = name, code = DefaultExpertTemplates.demoTradeJs)
        }
    }

    fun delete(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            scriptsRepo.delete(id)
        }
    }

    fun enableExclusive(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            scriptsRepo.enableExclusive(id)
        }
    }

    fun attach(scriptId: String, symbol: String, timeframe: String) {
        viewModelScope.launch(Dispatchers.IO) {
            attachRepo.attach(scriptId = scriptId, symbol = symbol, timeframe = timeframe, active = true)
        }
    }
}
