package com.tradingapp.metatrader.app.core.journal

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

data class JournalEntry(
    val timeMs: Long,
    val source: String,   // "EA", "OANDA", "SYSTEM"
    val level: String,    // "INFO","WARN","ERROR"
    val message: String
)

@Singleton
class LiveJournalBus @Inject constructor() {

    private val _flow = MutableSharedFlow<JournalEntry>(extraBufferCapacity = 512)
    val flow: SharedFlow<JournalEntry> = _flow

    fun post(source: String, level: String, message: String) {
        _flow.tryEmit(
            JournalEntry(
                timeMs = System.currentTimeMillis(),
                source = source,
                level = level,
                message = message
            )
        )
    }
}
