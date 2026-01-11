package com.tradingapp.metatrader.app.features.backtest.data

import android.content.Context
import com.tradingapp.metatrader.core.backtest.io.CsvBacktestCandleParser
import com.tradingapp.metatrader.domain.models.backtest.BacktestCandle
import java.io.BufferedReader
import java.io.InputStreamReader

class AssetBacktestCandleLoader(
    private val context: Context
) {
    fun loadCsv(pathInAssets: String): List<BacktestCandle> {
        return runCatching {
            context.assets.open(pathInAssets).use { input ->
                BufferedReader(InputStreamReader(input)).use { br ->
                    val text = br.readText()
                    CsvBacktestCandleParser.parse(text)
                }
            }
        }.getOrElse { emptyList() }
    }
}
