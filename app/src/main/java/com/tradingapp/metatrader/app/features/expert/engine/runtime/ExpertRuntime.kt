package com.tradingapp.metatrader.app.features.expert.engine.runtime

import com.tradingapp.metatrader.app.features.expert.engine.shared.BarSnapshot
import com.tradingapp.metatrader.app.features.expert.engine.shared.ExpertAction
import com.tradingapp.metatrader.app.features.expert.engine.shared.TickSnapshot

interface ExpertRuntime {
    fun init(expertCode: String, expertName: String, symbol: String, timeframe: String)
    fun onInit(): List<ExpertAction>
    fun onTick(tick: TickSnapshot): List<ExpertAction>
    fun onBar(bar: BarSnapshot): List<ExpertAction>
    fun close()
}
