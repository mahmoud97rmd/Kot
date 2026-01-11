package com.tradingapp.metatrader.app.features.expert.engine.shared

import com.tradingapp.metatrader.app.features.expert.data.ExpertAttachmentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttachedExpertResolver @Inject constructor(
    private val attachments: ExpertAttachmentRepository
) {
    /**
     * Returns the active scriptId attached to (symbol,timeframe) or null.
     */
    suspend fun resolveScriptId(symbol: String, timeframe: String): String? {
        val list = attachments.getAllNow()
        val att = list.firstOrNull { it.symbol == symbol && it.timeframe == timeframe && it.isActive }
        return att?.scriptId
    }
}
