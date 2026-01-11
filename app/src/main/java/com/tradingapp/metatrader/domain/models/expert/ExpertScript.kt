package com.tradingapp.metatrader.domain.models.expert

data class ExpertScript(
    val id: String,
    val name: String,
    val language: ExpertLanguage = ExpertLanguage.JAVASCRIPT,
    val code: String,
    val createdAtMs: Long,
    val updatedAtMs: Long,
    val isEnabled: Boolean = false
)

enum class ExpertLanguage { JAVASCRIPT }
