package com.tradingapp.metatrader.app.features.backtest.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.tradingapp.metatrader.app.features.backtest.export.html.BacktestHtmlReportBuilder
import com.tradingapp.metatrader.domain.models.backtest.BacktestResult
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Locale

class BacktestExporter(private val context: Context) {

    data class ExportedFiles(
        val csvUri: Uri,
        val jsonUri: Uri,
        val htmlUri: Uri
    )

    fun export(result: BacktestResult): ExportedFiles {
        val dir = File(context.cacheDir, "backtest_exports")
        if (!dir.exists()) dir.mkdirs()

        val csvFile = File(dir, "trades.csv")
        val jsonFile = File(dir, "report.json")
        val htmlFile = File(dir, "report.html")

        csvFile.writeText(buildCsv(result))
        jsonFile.writeText(buildJson(result))
        htmlFile.writeText(BacktestHtmlReportBuilder.build(result))

        val csvUri = fileUri(csvFile)
        val jsonUri = fileUri(jsonFile)
        val htmlUri = fileUri(htmlFile)

        return ExportedFiles(csvUri, jsonUri, htmlUri)
    }

    private fun buildCsv(result: BacktestResult): String {
        val sb = StringBuilder()
        sb.append("id,side,lots,entryTimeSec,entryPrice,exitTimeSec,exitPrice,profit,stopLoss,takeProfit\n")
        for (t in result.trades) {
            sb.append(t.id).append(',')
            sb.append(t.side.name).append(',')
            sb.append(String.format(Locale.US, "%.2f", t.lots)).append(',')
            sb.append(t.entryTimeSec).append(',')
            sb.append(String.format(Locale.US, "%.5f", t.entryPrice)).append(',')
            sb.append(t.exitTimeSec).append(',')
            sb.append(String.format(Locale.US, "%.5f", t.exitPrice)).append(',')
            sb.append(String.format(Locale.US, "%.2f", t.profit)).append(',')
            sb.append(t.stopLoss?.let { String.format(Locale.US, "%.5f", it) } ?: "").append(',')
            sb.append(t.takeProfit?.let { String.format(Locale.US, "%.5f", it) } ?: "")
            sb.append('\n')
        }
        return sb.toString()
    }

    private fun buildJson(result: BacktestResult): String {
        val root = JSONObject()

        root.put("config", JSONObject().apply {
            put("initialBalance", result.config.initialBalance)
            put("commissionPerLot", result.config.commissionPerLot)
            put("spreadPoints", result.config.spreadPoints)
            put("slippagePoints", result.config.slippagePoints)
            put("pointValue", result.config.pointValue)
            put("modelingMode", result.config.modelingMode.name)
            put("stopLossPoints", result.config.stopLossPoints)
            put("takeProfitPoints", result.config.takeProfitPoints)
            put("riskPercent", result.config.riskPercent)
        })

        root.put("metrics", JSONObject().apply {
            put("netProfit", result.metrics.netProfit)
            put("grossProfit", result.metrics.grossProfit)
            put("grossLoss", result.metrics.grossLoss)
            put("winRate", result.metrics.winRate)
            put("totalTrades", result.metrics.totalTrades)
            put("maxDrawdown", result.metrics.maxDrawdown)
            put("profitFactor", result.metrics.profitFactor)
            put("expectedPayoff", result.metrics.expectedPayoff)
            put("recoveryFactor", result.metrics.recoveryFactor)
            put("sharpeLike", result.metrics.sharpeLike)
        })

        val trades = JSONArray()
        for (t in result.trades) {
            trades.put(JSONObject().apply {
                put("id", t.id)
                put("side", t.side.name)
                put("lots", t.lots)
                put("entryTimeSec", t.entryTimeSec)
                put("entryPrice", t.entryPrice)
                put("exitTimeSec", t.exitTimeSec)
                put("exitPrice", t.exitPrice)
                put("profit", t.profit)
                put("stopLoss", t.stopLoss)
                put("takeProfit", t.takeProfit)
            })
        }
        root.put("trades", trades)

        return root.toString(2)
    }

    private fun fileUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            file
        )
    }
}
