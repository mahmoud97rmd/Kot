package com.tradingapp.metatrader.app.features.expert.engine.runtime

import com.tradingapp.metatrader.app.features.expert.engine.shared.BarSnapshot
import com.tradingapp.metatrader.app.features.expert.engine.shared.ExpertAction
import com.tradingapp.metatrader.app.features.expert.engine.shared.TickSnapshot
import org.mozilla.javascript.Context
import org.mozilla.javascript.Function
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.ScriptableObject
import java.util.concurrent.atomic.AtomicBoolean

class RhinoExpertRuntime : ExpertRuntime {

    private var cx: Context? = null
    private var scope: Scriptable? = null
    private var apiObj: ScriptableObject? = null

    private var fnOnInit: Function? = null
    private var fnOnTick: Function? = null
    private var fnOnBar: Function? = null

    private val initialized = AtomicBoolean(false)

    private val actions = ArrayList<ExpertAction>()

    override fun init(expertCode: String, expertName: String, symbol: String, timeframe: String) {
        if (initialized.getAndSet(true)) return

        val context = Context.enter()
        context.optimizationLevel = -1 // important for Android compatibility
        cx = context

        val sc = context.initStandardObjects()
        scope = sc

        val api = object : ScriptableObject() {
            override fun getClassName(): String = "Api"

            @Suppress("unused")
            fun jsFunction_log(level: String?, message: String?) {
                actions.add(ExpertAction.Log(level ?: "INFO", message ?: ""))
            }

            @Suppress("unused")
            fun jsFunction_buy(units: Any?, tp: Any?, sl: Any?) {
                val u = toLongSafe(units)
                actions.add(ExpertAction.MarketBuy(units = u, tp = toDoubleOrNull(tp), sl = toDoubleOrNull(sl)))
            }

            @Suppress("unused")
            fun jsFunction_sell(units: Any?, tp: Any?, sl: Any?) {
                val u = toLongSafe(units)
                actions.add(ExpertAction.MarketSell(units = u, tp = toDoubleOrNull(tp), sl = toDoubleOrNull(sl)))
            }

            @Suppress("unused")
            fun jsFunction_closeAll() {
                actions.add(ExpertAction.CloseAll)
            }
        }
        api.defineFunctionProperties(
            arrayOf("log", "buy", "sell", "closeAll"),
            api.javaClass,
            ScriptableObject.DONTENUM
        )
        apiObj = api
        ScriptableObject.putProperty(sc, "api", api)

        // Provide meta info
        ScriptableObject.putProperty(sc, "EA_NAME", expertName)
        ScriptableObject.putProperty(sc, "SYMBOL", symbol)
        ScriptableObject.putProperty(sc, "TIMEFRAME", timeframe)

        // Load helper wrapper + user code
        val wrapper = """
            // The EA must define onInit(api), onTick(tick, api), onBar(bar, api).
            if (typeof onInit !== 'function') { function onInit(api) {} }
            if (typeof onTick !== 'function') { function onTick(tick, api) {} }
            if (typeof onBar !== 'function') { function onBar(bar, api) {} }
        """.trimIndent()

        context.evaluateString(sc, wrapper, "wrapper", 1, null)
        context.evaluateString(sc, expertCode, "expert", 1, null)

        fnOnInit = ScriptableObject.getProperty(sc, "onInit") as? Function
        fnOnTick = ScriptableObject.getProperty(sc, "onTick") as? Function
        fnOnBar = ScriptableObject.getProperty(sc, "onBar") as? Function
    }

    override fun onInit(): List<ExpertAction> {
        return callNoArg(fnOnInit)
    }

    override fun onTick(tick: TickSnapshot): List<ExpertAction> {
        val sc = scope ?: return emptyList()
        val context = cx ?: return emptyList()
        val fn = fnOnTick ?: return emptyList()

        actions.clear()

        val tickObj = context.newObject(sc).apply {
            ScriptableObject.putProperty(this, "symbol", tick.symbol)
            ScriptableObject.putProperty(this, "timeEpochMs", tick.timeEpochMs)
            ScriptableObject.putProperty(this, "bid", tick.bid)
            ScriptableObject.putProperty(this, "ask", tick.ask)
            ScriptableObject.putProperty(this, "mid", tick.mid)
        }

        fn.call(context, sc, sc, arrayOf(tickObj, apiObj))
        return drain()
    }

    override fun onBar(bar: BarSnapshot): List<ExpertAction> {
        val sc = scope ?: return emptyList()
        val context = cx ?: return emptyList()
        val fn = fnOnBar ?: return emptyList()

        actions.clear()

        val barObj = context.newObject(sc).apply {
            ScriptableObject.putProperty(this, "symbol", bar.symbol)
            ScriptableObject.putProperty(this, "timeframe", bar.timeframe)
            ScriptableObject.putProperty(this, "openTimeSec", bar.openTimeSec)
            ScriptableObject.putProperty(this, "open", bar.open)
            ScriptableObject.putProperty(this, "high", bar.high)
            ScriptableObject.putProperty(this, "low", bar.low)
            ScriptableObject.putProperty(this, "close", bar.close)
        }

        fn.call(context, sc, sc, arrayOf(barObj, apiObj))
        return drain()
    }

    override fun close() {
        runCatching {
            fnOnInit = null
            fnOnTick = null
            fnOnBar = null
            scope = null
            apiObj = null
            cx?.let { Context.exit() }
            cx = null
        }
    }

    private fun callNoArg(fn: Function?): List<ExpertAction> {
        val sc = scope ?: return emptyList()
        val context = cx ?: return emptyList()
        val f = fn ?: return emptyList()

        actions.clear()
        f.call(context, sc, sc, arrayOf(apiObj))
        return drain()
    }

    private fun drain(): List<ExpertAction> {
        if (actions.isEmpty()) return emptyList()
        return actions.toList().also { actions.clear() }
    }

    private fun toLongSafe(v: Any?): Long {
        return when (v) {
            is Number -> v.toLong()
            is String -> v.toLongOrNull() ?: 0L
            else -> 0L
        }
    }

    private fun toDoubleOrNull(v: Any?): Double? {
        return when (v) {
            null -> null
            is Number -> v.toDouble()
            is String -> v.toDoubleOrNull()
            else -> null
        }
    }
}
