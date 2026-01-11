package com.tradingapp.metatrader.app.core.expert.supervisor

import com.tradingapp.metatrader.app.features.journal.logs.ExpertsBus
import com.tradingapp.metatrader.app.features.journal.logs.JournalBus
import com.tradingapp.metatrader.app.features.journal.logs.LogEntry
import com.tradingapp.metatrader.app.features.journal.logs.LogLevel

class BusLogSink(
    private val journal: JournalBus,
    private val experts: ExpertsBus,
    private val tag: String
) : ExpertLogSink {

    override fun log(text: String) {
        val e = LogEntry(
            timeMs = System.currentTimeMillis(),
            level = LogLevel.INFO,
            tag = tag,
            message = text
        )
        journal.append(e)
        experts.append(e)
    }

    fun logExpert(symbol: String, timeframe: String, expertName: String, level: LogLevel, msg: String) {
        val e = LogEntry(
            timeMs = System.currentTimeMillis(),
            level = level,
            tag = tag,
            message = msg,
            symbol = symbol,
            timeframe = timeframe,
            expertName = expertName
        )
        journal.append(e)
        experts.append(e)
    }
}

object NoopMarkerSink : ExpertMarkerSink {
    override fun onBuy(timeSec: Long, text: String) {}
    override fun onSell(timeSec: Long, text: String) {}
    override fun onClose(timeSec: Long, text: String) {}
}
