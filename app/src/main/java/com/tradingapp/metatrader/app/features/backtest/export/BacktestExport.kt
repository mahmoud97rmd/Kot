package com.tradingapp.metatrader.app.features.backtest.export

import android.content.Context
import androidx.core.content.FileProvider
import com.tradingapp.metatrader.domain.models.backtest.BacktestResult
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BacktestExport {

    fun writeCsvToCache(context: Context, result: BacktestResult): File {
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "backtest_${result.instrument}_$ts.csv"
        val f = File(context.cacheDir, fileName)

        val sb = StringBuilder()
        sb.append("instrument,totalTrades,netProfit,winRate,maxDrawdown\n")
        sb.append(result.instrument).append(",")
            .append(result.totalTrades).append(",")
            .append(result.netProfit).append(",")
            .append(result.winRate).append(",")
            .append(result.maxDrawdown).append("\n\n")

        sb.append("equityCurve\n")
        for (v in result.equityCurve) {
            sb.append(v).append("\n")
        }

        f.writeText(sb.toString())
        return f
    }

    fun uriForFile(context: Context, file: File) =
        FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
}
