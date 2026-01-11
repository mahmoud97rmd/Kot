package com.tradingapp.metatrader.domain.models.expert

data class ExpertAttachment(
    val id: String,
    val scriptId: String,
    val symbol: String,
    val timeframe: String,
    val isActive: Boolean,
    val createdAtMs: Long,
    val updatedAtMs: Long
)
