package com.tradingapp.metatrader.app.features.expert.runtime.hosts

import com.tradingapp.metatrader.app.core.expert.runtime.ExpertRuntime
import com.tradingapp.metatrader.app.features.expert.runtime.ExpertSnapshot
import com.tradingapp.metatrader.app.features.expert.runtime.api.ExpertTradingApi
import com.tradingapp.metatrader.app.features.expert.runtime.bridge.ExpertCall
import com.tradingapp.metatrader.app.features.expert.runtime.bridge.ExpertCallResult
import com.tradingapp.metatrader.domain.models.backtest.BacktestSide

/**
 * Host مسؤول عن:
 * - تشغيل runtime
 * - حقن snapshot
 * - تنفيذ calls queued
 * - دفع results إلى JS
 *
 * يستخدم ExpertTradingApi التي ستربطها لاحقاً بـ VirtualExchange / BacktestEngine.
 */
class BacktestExpertHost(
    private val api: ExpertTradingApi
) : AutoCloseable {

    private val rt = ExpertRuntime()
    private var loaded = false

    fun load(scriptCode: String) {
        rt.load(scriptCode)
        loaded = true
    }

    fun deinit() {
        if (!loaded) return
        runCatching { rt.deinit() }
    }

    fun onTick(nowSec: Long, bid: Double, ask: Double) {
        if (!loaded) return
        val snap = ExpertSnapshot(
            symbol = api.symbol(),
            timeframe = api.timeframe(),
            nowSec = nowSec,
            bid = bid,
            ask = ask,
            positionsTotal = api.positionsTotal()
        )
        rt.onTick(snap)
        processCalls()
    }

    fun onBar(barTimeSec: Long, o: Double, h: Double, l: Double, c: Double, bid: Double, ask: Double) {
        if (!loaded) return
        val snap = ExpertSnapshot(
            symbol = api.symbol(),
            timeframe = api.timeframe(),
            nowSec = barTimeSec,
            bid = bid,
            ask = ask,
            positionsTotal = api.positionsTotal()
        )
        rt.onBar(snap, barTimeSec, o, h, l, c)
        processCalls()
    }

    private fun processCalls() {
        val calls = rt.drainCalls()
        if (calls.isEmpty()) return

        val results = ArrayList<ExpertCallResult>(calls.size)

        for (c in calls) {
            when (c) {
                is ExpertCall.Log -> {
                    api.log(c.level, c.message)
                    results.add(ExpertCallResult.Ok(c.id, valueJson = "true"))
                }

                is ExpertCall.OrderSend -> {
                    val side = when (c.side.uppercase()) {
                        "BUY" -> BacktestSide.BUY
                        "SELL" -> BacktestSide.SELL
                        else -> BacktestSide.BUY
                    }

                    try {
                        val posId = api.orderSend(
                            side = side,
                            lots = c.lots,
                            sl = c.sl,
                            tp = c.tp,
                            comment = c.comment
                        )
                        // posId as JSON string
                        results.add(ExpertCallResult.Ok(c.id, valueJson = json(posId)))
                    } catch (e: Throwable) {
                        results.add(ExpertCallResult.Error(c.id, message = e.message ?: "orderSend failed"))
                    }
                }

                is ExpertCall.PositionClose -> {
                    try {
                        val ok = api.positionClose(c.positionId)
                        results.add(ExpertCallResult.Ok(c.id, valueJson = if (ok) "true" else "false"))
                    } catch (e: Throwable) {
                        results.add(ExpertCallResult.Error(c.id, message = e.message ?: "positionClose failed"))
                    }
                }
            }
        }

        rt.pushResults(results)
    }

    override fun close() {
        runCatching { deinit() }
        runCatching { rt.close() }
    }

    private fun json(s: String): String =
        "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
}
