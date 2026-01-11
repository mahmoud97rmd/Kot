package com.tradingapp.metatrader.app.features.tester.export

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ExportDealRow(
    val symbol: String,
    val side: String,
    val lots: Double,
    val entryPrice: Double,
    val exitPrice: Double,
    val openTimeSec: Long,
    val closeTimeSec: Long,
    val profit: Double,
    val reason: String
)

object BacktestCsvExporter {

    fun exportDeals(context: Context, namePrefix: String, deals: List<ExportDealRow>): File {
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(context.cacheDir, "${namePrefix}_deals_${ts}.csv")

        file.bufferedWriter().use { w ->
            w.appendLine("symbol,side,lots,entryPrice,exitPrice,openTimeSec,closeTimeSec,profit,reason")
            for (d in deals) {
                w.appendLine(
                    listOf(
                        d.symbol,
                        d.side,
                        d.lots.toString(),
                        d.entryPrice.toString(),
                        d.exitPrice.toString(),
                        d.openTimeSec.toString(),
                        d.closeTimeSec.toString(),
                        d.profit.toString(),
                        d.reason
                    ).joinToString(",")
                )
            }
        }
        return file
    }
}
