package com.tradingapp.metatrader.app.core.expert.runtime

import com.tradingapp.metatrader.app.core.candles.Candle
import com.tradingapp.metatrader.app.core.expert.dsl.ExpertInterpreterMt5
import com.tradingapp.metatrader.app.core.trading.mt5sim.VirtualAccountMt5

class ExpertRuntimeMt5(
    scriptText: String,
    private val account: VirtualAccountMt5
) {
    data class Event(
        val message: String,
        val markerJson: String? = null
    )

    private val interpreter = ExpertInterpreterMt5(scriptText, account)

    fun onClosedBar(symbol: String, timeframe: String, candle: Candle): List<Event> {
        return interpreter.onClosedBar(symbol, timeframe, candle)
    }
}
