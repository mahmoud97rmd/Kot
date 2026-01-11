package com.tradingapp.metatrader.app.features.backtest.trades

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tradingapp.metatrader.app.R
import com.tradingapp.metatrader.app.core.storage.SafTextWriter
import com.tradingapp.metatrader.app.features.backtest.export.TradesExportBuilder

class BacktestTradesActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_TRADES_JSON = "tradesJson"
        const val EXTRA_SUMMARY_JSON = "summaryJson"
        const val EXTRA_CONFIG_JSON = "configJson"
    }

    private var pendingContent: String? = null
    private var pendingMime: String = "text/plain"
    private var pendingFilename: String = "report.txt"

    private val createDocLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri: Uri? ->
            if (uri == null) return@registerForActivityResult
            val text = pendingContent ?: return@registerForActivityResult
            SafTextWriter.writeText(contentResolver, uri, text)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backtest_trades)

        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Backtest Trades"
        val tradesJson = intent.getStringExtra(EXTRA_TRADES_JSON) ?: "[]"
        val summaryJson = intent.getStringExtra(EXTRA_SUMMARY_JSON) ?: "{}"
        val configJson = intent.getStringExtra(EXTRA_CONFIG_JSON) ?: "{}"

        val titleText: TextView = findViewById(R.id.titleText)
        val subText: TextView = findViewById(R.id.subText)

        val exportCsvBtn: Button = findViewById(R.id.exportCsvBtn)
        val exportJsonBtn: Button = findViewById(R.id.exportJsonBtn)
        val exportHtmlBtn: Button = findViewById(R.id.exportHtmlBtn)

        val list: RecyclerView = findViewById(R.id.list)
        val adapter = TradesAdapter()

        titleText.text = title
        subText.text = "Trades: ${TradesJsonParser.parse(tradesJson).size}"

        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter
        adapter.submit(TradesJsonParser.parse(tradesJson))

        exportCsvBtn.setOnClickListener {
            pendingMime = "text/csv"
            pendingFilename = safeFileName(title) + ".csv"
            pendingContent = TradesExportBuilder.csvFromTradesJson(tradesJson)
            createDocLauncher.launch(pendingFilename)
        }

        exportJsonBtn.setOnClickListener {
            pendingMime = "application/json"
            pendingFilename = safeFileName(title) + ".json"
            pendingContent = TradesExportBuilder.jsonReport(title, summaryJson, configJson, tradesJson)
            createDocLauncher.launch(pendingFilename)
        }

        exportHtmlBtn.setOnClickListener {
            pendingMime = "text/html"
            pendingFilename = safeFileName(title) + ".html"
            pendingContent = TradesExportBuilder.htmlReport(title, summaryJson, tradesJson)
            createDocLauncher.launch(pendingFilename)
        }
    }

    private fun safeFileName(s: String): String {
        return s.replace(Regex("[^a-zA-Z0-9._-]+"), "_").take(80).ifBlank { "backtest_report" }
    }
}
