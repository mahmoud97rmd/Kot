package com.tradingapp.metatrader.app.features.tester.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tradingapp.metatrader.app.R
import com.tradingapp.metatrader.app.features.chart.ChartActivity
import com.tradingapp.metatrader.app.features.tester.engine.BacktestRunner
import com.tradingapp.metatrader.app.features.tester.engine.OandaHistoryDownloader
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToLong

@AndroidEntryPoint
class StrategyTesterActivity : AppCompatActivity() {

    @Inject lateinit var runner: BacktestRunner
    @Inject lateinit var downloader: OandaHistoryDownloader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_strategy_tester)

        val symbolEdit: EditText = findViewById(R.id.symbolEdit)
        val tfEdit: EditText = findViewById(R.id.timeframeEdit)
        val countEdit: EditText = findViewById(R.id.countEdit)
        val downloadBtn: Button = findViewById(R.id.downloadBtn)

        val scriptEdit: EditText = findViewById(R.id.scriptEdit)
        val runBtn: Button = findViewById(R.id.runBacktestBtn)
        val visualBtn: Button = findViewById(R.id.visualModeBtn)

        val statusText: TextView = findViewById(R.id.statusText)
        val reportText: TextView = findViewById(R.id.reportText)
        val tradesList: ListView = findViewById(R.id.tradesList)

        if (scriptEdit.text.isNullOrBlank()) {
            scriptEdit.setText(
                """
                # name: EMA Cross Demo
                input lot=0.10

                rule BUY when ema(20) crosses_above ema(50)
                rule SELL when ema(20) crosses_below ema(50)
                """.trimIndent()
            )
        }

        fun readSymbol(): String = symbolEdit.text.toString().trim().ifBlank { "XAU_USD" }
        fun readTf(): String = tfEdit.text.toString().trim().uppercase().ifBlank { "M1" }
        fun readCount(): Int = countEdit.text.toString().trim().toIntOrNull()?.coerceIn(50, 5000) ?: 2000

        downloadBtn.setOnClickListener {
            val symbol = readSymbol()
            val tf = readTf()
            val count = readCount()

            statusText.text = "Status: Downloading..."
            reportText.text = "Downloading from OANDA..."
            tradesList.adapter = null

            lifecycleScope.launch {
                try {
                    val saved = downloader.downloadIntoCache(symbol, tf, count) { msg ->
                        runOnUiThread { statusText.text = "Status: $msg" }
                    }
                    reportText.text = "Downloaded & cached: $saved candles."
                    statusText.text = "Status: Ready."
                } catch (t: Throwable) {
                    statusText.text = "Status: Error"
                    reportText.text = "Download error: ${t.message}"
                }
            }
        }

        runBtn.setOnClickListener {
            val symbol = readSymbol()
            val tf = readTf()
            val count = readCount()
            val script = scriptEdit.text.toString()

            statusText.text = "Status: Running backtest..."
            reportText.text = "Working..."
            tradesList.adapter = null

            lifecycleScope.launch {
                try {
                    val rep = runner.run(symbol, tf, script, renderCount = count)

                    reportText.text = buildString {
                        append("Symbol: ").append(rep.symbol).append("\n")
                        append("Timeframe: ").append(rep.timeframe).append("\n")
                        append("Candles: ").append(rep.candles).append("\n")
                        append("Trades: ").append(rep.trades.size).append("\n\n")
                        append("Net Profit: ").append(fmt2(rep.netProfit)).append("\n")
                        append("Max Drawdown: ").append(fmt2(rep.maxDrawdown)).append("\n")
                        append("Win Rate: ").append(fmt2(rep.winRate)).append("%\n")
                    }

                    val items = rep.trades.map { t ->
                        val side = t.side.name
                        val p = fmt2(t.profit)
                        val entry = fmt5(t.entryPrice)
                        val exit = fmt5(t.exitPrice)
                        "[$side] $p | entry=$entry exit=$exit"
                    }
                    tradesList.adapter = ArrayAdapter(this@StrategyTesterActivity, android.R.layout.simple_list_item_1, items)

                    statusText.text = "Status: Done."
                } catch (t: Throwable) {
                    statusText.text = "Status: Error"
                    reportText.text = "Error: ${t.message}"
                }
            }
        }

        visualBtn.setOnClickListener {
            val symbol = readSymbol()
            val tf = readTf()
            val count = readCount()
            val script = scriptEdit.text.toString()

            statusText.text = "Status: Preparing visual mode..."
            reportText.text = "Ensuring history is cached..."

            lifecycleScope.launch {
                try {
                    downloader.downloadIntoCache(symbol, tf, count) { msg ->
                        runOnUiThread { statusText.text = "Status: $msg" }
                    }

                    val it = Intent(this@StrategyTesterActivity, ChartActivity::class.java).apply {
                        putExtra(ChartActivity.EXTRA_SYMBOL, symbol)
                        putExtra(ChartActivity.EXTRA_TIMEFRAME, tf)
                        putExtra(ChartActivity.EXTRA_VISUAL_SCRIPT, script)
                        putExtra(ChartActivity.EXTRA_VISUAL_AUTOSTART, true)
                    }
                    startActivity(it)

                    statusText.text = "Status: Opened visual mode."
                } catch (t: Throwable) {
                    statusText.text = "Status: Error"
                    reportText.text = "Visual prepare error: ${t.message}"
                }
            }
        }
    }

    private fun fmt2(v: Double): String = ((v * 100.0).roundToLong() / 100.0).toString()
    private fun fmt5(v: Double): String = ((v * 100000.0).roundToLong() / 100000.0).toString()
}
