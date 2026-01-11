package com.tradingapp.metatrader.app.core.trading.positions

data class OpenPositionSummary(
    val instrument: String,
    val longUnits: Long,
    val shortUnits: Long
) {
    val netUnits: Long get() = longUnits + shortUnits
    val hasAny: Boolean get() = (longUnits != 0L || shortUnits != 0L)
}

data class CloseResult(
    val ok: Boolean,
    val message: String,
    val raw: String? = null
)

interface PositionService {
    suspend fun getOpenPositions(): List<OpenPositionSummary>
    suspend fun closeInstrumentAll(instrument: String): CloseResult
}
