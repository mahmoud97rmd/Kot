package com.tradingapp.metatrader.app.features.backtest.export

import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

object TradesExportBuilder {

    fun csvFromTradesJson(tradesJson: String): String {
        val arr = runCatching { JSONArray(tradesJson) }.getOrNull() ?: JSONArray()
        val sb = StringBuilder()
        sb.append("id,side,entryTimeSec,exitTimeSec,entryPrice,exitPrice,profit,reason\n")
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            sb.append(csv(o.optString("id"))).append(',')
            sb.append(csv(o.optString("side"))).append(',')
            sb.append(o.optLong("entryTimeSec")).append(',')
            sb.append(o.optLong("exitTimeSec")).append(',')
            sb.append(fmt(o.optDouble("entryPrice"))).append(',')
            sb.append(fmt(o.optDouble("exitPrice"))).append(',')
            sb.append(fmt(o.optDouble("profit"))).append(',')
            sb.append(csv(o.optString("reason")))
            sb.append('\n')
        }
        return sb.toString()
    }

    fun jsonReport(title: String, summaryJson: String, configJson: String, tradesJson: String): String {
        val out = JSONObject()
        out.put("title", title)
        out.put("summary", runCatching { JSONObject(summaryJson) }.getOrElse { JSONObject() })
        out.put("config", runCatching { JSONObject(configJson) }.getOrElse { JSONObject() })
        out.put("trades", runCatching { JSONArray(tradesJson) }.getOrElse { JSONArray() })
        return out.toString(2)
    }

    fun htmlReport(title: String, summaryJson: String, tradesJson: String): String {
        val sum = runCatching { JSONObject(summaryJson) }.getOrElse { JSONObject() }
        val arr = runCatching { JSONArray(tradesJson) }.getOrElse { JSONArray() }

        val totalTrades = sum.optInt("totalTrades", arr.length())
        val winRatePct = sum.optDouble("winRate", 0.0) * 100.0
        val netProfit = sum.optDouble("netProfit", 0.0)
        val maxDd = sum.optDouble("maxDrawdown", 0.0)

        val header = """
<!doctype html>
<html>
<head>
<meta charset="utf-8"/>
<meta name="viewport" content="width=device-width,initial-scale=1"/>
<title>${esc(title)}</title>
<style>
 body{font-family:Arial, sans-serif;background:#0b1220;color:#d1d4dc;padding:16px;}
 h1{font-size:18px;margin:0 0 10px 0;}
 .meta{color:#8aa0c6;font-size:12px;margin-bottom:14px;}
 table{width:100%;border-collapse:collapse;font-size:12px;}
 th,td{border:1px solid #1f2a40;padding:8px;text-align:left;}
 th{background:#121a2b;}
 .pos{color:#4caf50;font-weight:bold;}
 .neg{color:#ff5252;font-weight:bold;}
</style>
</head>
<body>
<h1>${esc(title)}</h1>
<div class="meta">
Total Trades: $totalTrades |
Win Rate: ${String.format(Locale.US,"%.2f", winRatePct)}% |
Net Profit: ${String.format(Locale.US,"%.2f", netProfit)} |
Max Drawdown: ${String.format(Locale.US,"%.2f", maxDd)}
</div>
<table>
<thead>
<tr>
<th>ID</th><th>Side</th><th>Entry Time</th><th>Exit Time</th>
<th>Entry</th><th>Exit</th><th>Profit</th><th>Reason</th>
</tr>
</thead>
<tbody>
"""

        val rows = StringBuilder()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val profit = o.optDouble("profit", 0.0)
            val cls = if (profit >= 0) "pos" else "neg"

            rows.append("<tr>")
            rows.append("<td>").append(esc(o.optString("id"))).append("</td>")
            rows.append("<td>").append(esc(o.optString("side"))).append("</td>")
            rows.append("<td>").append(o.optLong("entryTimeSec")).append("</td>")
            rows.append("<td>").append(o.optLong("exitTimeSec")).append("</td>")
            rows.append("<td>").append(String.format(Locale.US,"%.2f", o.optDouble("entryPrice"))).append("</td>")
            rows.append("<td>").append(String.format(Locale.US,"%.2f", o.optDouble("exitPrice"))).append("</td>")
            rows.append("<td class=\"$cls\">").append(String.format(Locale.US,"%.2f", profit)).append("</td>")
            rows.append("<td>").append(esc(o.optString("reason"))).append("</td>")
            rows.append("</tr>")
        }

        val footer = """
</tbody>
</table>
</body>
</html>
"""
        return header + rows.toString() + footer
    }

    private fun fmt(x: Double): String = String.format(Locale.US, "%.2f", x)

    private fun csv(s: String): String {
        val needsQuote = s.contains(',') || s.contains('"') || s.contains('\n') || s.contains('\r')
        val v = s.replace("\"", "\"\"")
        return if (needsQuote) "\"$v\"" else v
    }

    private fun esc(s: String): String {
        return s.replace("&","&")
            .replace("<","<")
            .replace(">",">")
            .replace("\"",""")
            .replace("'","'")
    }
}
