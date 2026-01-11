package com.tradingapp.metatrader.app.core.expert.runtime

import com.tradingapp.metatrader.app.core.candles.Candle
import com.tradingapp.metatrader.app.core.expert.dsl.ExpertAction
import com.tradingapp.metatrader.app.core.expert.dsl.ExpertCondition
import com.tradingapp.metatrader.app.core.expert.dsl.ExpertScriptModel
import com.tradingapp.metatrader.app.core.trading.sim.Side
import com.tradingapp.metatrader.app.core.trading.sim.VirtualAccount

class ExpertRuntime(
    private val model: ExpertScriptModel,
    private val account: VirtualAccount
) {
    private val emaTrackers: MutableMap<Int, EmaTracker> = linkedMapOf()

    private var prevEma: MutableMap<Int, Double?> = linkedMapOf()

    data class Event(
        val timeSec: Long,
        val type: String,
        val message: String
    )

    fun onCandle(symbol: String, timeframe: String, candle: Candle): List<Event> {
        // update EMA trackers needed by rules
        val requiredPeriods = requiredEmaPeriods(model)
        for (p in requiredPeriods) {
            val tr = emaTrackers.getOrPut(p) { EmaTracker(p) }
            val prev = tr.valueOrNull()
            val now = tr.update(candle.close)
            prevEma[p] = prev
            // store current value implicitly in tracker
        }

        val events = ArrayList<Event>()

        // Evaluate rules
        for (r in model.rules) {
            val ok = evalCondition(r.condition, candle)
            if (!ok) continue

            when (r.action) {
                ExpertAction.BUY -> {
                    val lots = model.inputs["lot"] ?: 0.10
                    account.open(Side.BUY, candle.close, lots, candle.timeSec)
                    events.add(Event(candle.timeSec, "ORDER", "BUY opened @${candle.close} lots=$lots"))
                }
                ExpertAction.SELL -> {
                    val lots = model.inputs["lot"] ?: 0.10
                    account.open(Side.SELL, candle.close, lots, candle.timeSec)
                    events.add(Event(candle.timeSec, "ORDER", "SELL opened @${candle.close} lots=$lots"))
                }
                ExpertAction.CLOSE_ALL -> {
                    val closed = account.closeAll(candle.close, candle.timeSec)
                    if (closed.isNotEmpty()) {
                        val sum = closed.sumOf { it.profit }
                        events.add(Event(candle.timeSec, "CLOSE", "CLOSE_ALL @${candle.close} trades=${closed.size} profit=$sum"))
                    }
                }
            }
        }

        // Optional: periodic info (can be removed)
        return events
    }

    private fun evalCondition(cond: ExpertCondition, candle: Candle): Boolean {
        return when (cond) {
            is ExpertCondition.CloseGreaterThanOpen -> candle.close > candle.open
            is ExpertCondition.ProfitGreaterThan -> account.floatingPnL(candle.close) > cond.amount
            is ExpertCondition.EmaGreater -> {
                val a = emaTrackers[cond.a]?.valueOrNull() ?: return false
                val b = emaTrackers[cond.b]?.valueOrNull() ?: return false
                a > b
            }
            is ExpertCondition.EmaCrossAbove -> {
                val aNow = emaTrackers[cond.a]?.valueOrNull() ?: return false
                val bNow = emaTrackers[cond.b]?.valueOrNull() ?: return false
                val aPrev = prevEma[cond.a] ?: return false
                val bPrev = prevEma[cond.b] ?: return false
                (aPrev <= bPrev) && (aNow > bNow)
            }
            is ExpertCondition.EmaCrossBelow -> {
                val aNow = emaTrackers[cond.a]?.valueOrNull() ?: return false
                val bNow = emaTrackers[cond.b]?.valueOrNull() ?: return false
                val aPrev = prevEma[cond.a] ?: return false
                val bPrev = prevEma[cond.b] ?: return false
                (aPrev >= bPrev) && (aNow < bNow)
            }
        }
    }

    private fun requiredEmaPeriods(m: ExpertScriptModel): Set<Int> {
        val set = linkedSetOf<Int>()
        for (r in m.rules) {
            when (val c = r.condition) {
                is ExpertCondition.EmaGreater -> { set.add(c.a); set.add(c.b) }
                is ExpertCondition.EmaCrossAbove -> { set.add(c.a); set.add(c.b) }
                is ExpertCondition.EmaCrossBelow -> { set.add(c.a); set.add(c.b) }
                else -> {}
            }
        }
        return set
    }
}
