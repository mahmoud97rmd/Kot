package com.tradingapp.metatrader.app.features.journal.logs

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JournalBus @Inject constructor() {

    private val _entries = MutableStateFlow<List<LogEntry>>(emptyList())
    val entries: StateFlow<List<LogEntry>> = _entries.asStateFlow()

    @Synchronized
    fun append(entry: LogEntry) {
        val cur = _entries.value
        val next = (cur + entry).takeLast(5000)
        _entries.value = next
    }

    @Synchronized
    fun clear() {
        _entries.value = emptyList()
    }
}
