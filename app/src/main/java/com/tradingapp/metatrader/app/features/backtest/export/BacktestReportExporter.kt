package com.tradingapp.metatrader.app.features.backtest.export

import com.tradingapp.metatrader.domain.models.backtest.BacktestResult
import java.util.Locale

object BacktestReportExporter {

    fun toCsv(result: BacktestResult): String {
        val sb = StringBuilder()
        sb.append("id,side,entryTimeSec,exitTimeSec,entryPrice,exitPrice,profit,reason\n")
        for (t in result.trades) {
            sb.append(csv(t.id)).append(',')
            sb.append(csv(t.side)).append(',')
            sb.append(t.entryTimeSec).append(',')
            sb.append(t.exitTimeSec).append(',')
            sb.append(fmt(t.entryPrice)).append(',')
            sb.append(fmt(t.exitPrice)).append(',')
            sb.append(fmt(t.profit)).append(',')
            sb.append(csv(t.reason))
            sb.append('\n')
        }
        return sb.toString()
    }

    fun toJson(result: BacktestResult): String {
        // Minimal JSON manually to avoid extra libs
        val sb = StringBuilder()
        sb.append("{")
        sb.append("\"summary\":{")
        sb.append("\"totalTrades\":").append(result.totalTrades).append(',')
        sb.append("\"winRate\":").append(result.winRate).append(',')
        sb.append("\"netProfit\":").append(result.netProfit).append(',')
        sb.append("\"maxDrawdown\":").append(result.maxDrawdown)
        sb.append("},")
        sb.append("\"trades\":[")
        result.trades.forEachIndexed { idx, t ->
            if (idx > 0) sb.append(',')
            sb.append("{")
            sb.append("\"id\":").append(q(t.id)).append(',')
            sb.append("\"side\":").append(q(t.side)).append(',')
            sb.append("\"entryTimeSec\":").append(t.entryTimeSec).append(',')
            sb.append("\"exitTimeSec\":").append(t.exitTimeSec).append(',')
            sb.append("\"entryPrice\":").append(t.entryPrice).append(',')
            sb.append("\"exitPrice\":").append(t.exitPrice).append(',')
            sb.append("\"profit\":").append(t.profit).append(',')
            sb.append("\"reason\":").append(q(t.reason))
            sb.append("}")
        }
        sb.append("]")
        sb.append("}")
        return sb.toString()
    }

    fun toHtml(result: BacktestResult, title: String): String {
        val header = """
<!doctype html>
<html>
<head>
<meta charset="utf-8"/>
<meta name="viewport" content="width=device-width,initial-scale=1"/>
<title>${escapeHtml(title)}</title>
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
<h1>${escapeHtml(title)}</h1>
<div class="meta">
Total Trades: ${result.totalTrades} |
Win Rate: ${String.format(Locale.US,"%.2f", result.winRate*100)}% |
Net Profit: ${fmt(result.netProfit)} |
Max Drawdown: ${fmt(result.maxDrawdown)}
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
        for (t in result.trades) {
            val cls = if (t.profit >= 0) "pos" else "neg"
            rows.append("<tr>")
            rows.append("<td>").append(escapeHtml(t.id)).append("</td>")
            rows.append("<td>").append(escapeHtml(t.side)).append("</td>")
            rows.append("<td>").append(t.entryTimeSec).append("</td>")
            rows.append("<td>").append(t.exitTimeSec).append("</td>")
            rows.append("<td>").append(fmt(t.entryPrice)).append("</td>")
            rows.append("<td>").append(fmt(t.exitPrice)).append("</td>")
            rows.append("<td class=\"$cls\">").append(fmt(t.profit)).append("</td>")
            rows.append("<td>").append(escapeHtml(t.reason)).append("</td>")
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

    private fun q(s: String): String = "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

    private fun escapeHtml(s: String): String {
        return s.replace("&","&")
            .replace("<","<")
            .replace(">",">")
            .replace("\"",""")
            .replace("'","'")
    }
}
