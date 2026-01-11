package com.tradingapp.metatrader.app.features.expert.runtime.bridge

sealed class ExpertCall {
    abstract val id: String

    data class Log(
        override val id: String,
        val level: String,
        val message: String
    ) : ExpertCall()

    data class OrderSend(
        override val id: String,
        val side: String,
        val lots: Double,
        val sl: Double?,
        val tp: Double?,
        val comment: String?
    ) : ExpertCall()

    data class PositionClose(
        override val id: String,
        val positionId: String
    ) : ExpertCall()
}

sealed class ExpertCallResult {
    abstract val id: String

    data class Ok(
        override val id: String,
        val valueJson: String // JSON encoded value
    ) : ExpertCallResult()

    data class Error(
        override val id: String,
        val message: String
    ) : ExpertCallResult()
}
