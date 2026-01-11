package com.tradingapp.metatrader.app.features.tester.export

/**
 * انسخ هذا الجزء داخل StrategyTesterActivity الحقيقي عندك.
 *
 * val exportBtn: Button = findViewById(R.id.exportBtn)
 * exportBtn.setOnClickListener {
 *    val deals = lastBacktestDeals.map { d ->
 *        ExportDealRow(
 *          symbol = d.symbol,
 *          side = d.side.name,
 *          lots = d.lots,
 *          entryPrice = d.entryPrice,
 *          exitPrice = d.exitPrice,
 *          openTimeSec = d.openTimeSec,
 *          closeTimeSec = d.closeTimeSec,
 *          profit = d.profit,
 *          reason = d.reason
 *        )
 *    }
 *    val file = BacktestCsvExporter.exportDeals(this, "backtest_${symbol}_${timeframe}", deals)
 *    Toast.makeText(this, "Exported: ${file.absolutePath}", Toast.LENGTH_LONG).show()
 * }
 */
object StrategyTesterExportSnippet
