package com.tradingapp.metatrader.app.features.expert.engine.shared

object ExpertCodeComposer {
    fun compose(expertCode: String, inputsJson: String): String {
        val safeJson = inputsJson.trim().ifBlank { "{}" }
        // We inject a global object "INPUTS" accessible to the EA.
        val header = "var INPUTS = $safeJson;\n"
        return header + expertCode
    }
}
