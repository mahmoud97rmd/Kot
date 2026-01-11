package com.tradingapp.metatrader.app.features.expert.engine.shared

import com.tradingapp.metatrader.app.features.expert.data.ExpertScriptRepository
import com.tradingapp.metatrader.app.features.expert.inputs.ExpertInputsStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpertCodeProvider @Inject constructor(
    private val scripts: ExpertScriptRepository,
    private val inputs: ExpertInputsStore
) {
    suspend fun getComposedCode(scriptId: String): String {
        val s = scripts.getById(scriptId)
        val raw = s?.code ?: ""
        val inJson = runCatching { inputs.getInputsJson(scriptId) }.getOrElse { "{}" }
        return ExpertCodeComposer.compose(raw, inJson)
    }
}
