package com.tradingapp.metatrader.app.core.expert.dsl

class ExpertDslParser {

    fun parse(scriptText: String): ExpertScriptModel {
        val rawLines = scriptText.lines()

        var name = "Unnamed Expert"
        val inputs = LinkedHashMap<String, Double>()
        val rules = ArrayList<ExpertRule>()

        for ((idx0, raw) in rawLines.withIndex()) {
            val lineNo = idx0 + 1
            val ln = raw.trim()
            if (ln.isBlank()) continue
            if (ln.startsWith("#")) continue

            try {
                if (ln.startsWith("name:", ignoreCase = true)) {
                    name = ln.substringAfter(":", "").trim().ifBlank { name }
                    continue
                }

                if (ln.startsWith("input ", ignoreCase = true)) {
                    val kv = ln.substringAfter("input", "").trim()
                    val key = kv.substringBefore("=").trim()
                    val valStr = kv.substringAfter("=", "").trim()
                    val v = valStr.toDoubleOrNull()
                        ?: throw ExpertDslParseException(lineNo, "Invalid input value: '$valStr'")
                    if (key.isBlank()) throw ExpertDslParseException(lineNo, "Invalid input key")
                    inputs[key] = v
                    continue
                }

                if (ln.startsWith("rule ", ignoreCase = true)) {
                    val rest = ln.substringAfter("rule", "").trim()
                    val actionStr = rest.substringBefore(" ", "").trim().uppercase()
                    val action = runCatching { ExpertAction.valueOf(actionStr) }
                        .getOrElse { throw ExpertDslParseException(lineNo, "Unknown action '$actionStr'") }

                    val whenPart = rest.substringAfter("when", missingDelimiterValue = "").trim()
                    if (whenPart.isBlank()) throw ExpertDslParseException(lineNo, "Missing 'when' condition")

                    val cond = parseCondition(whenPart, lineNo)
                    rules.add(ExpertRule(action, cond))
                    continue
                }

                throw ExpertDslParseException(lineNo, "Unknown statement: '$ln'")
            } catch (e: ExpertDslParseException) {
                throw e
            } catch (t: Throwable) {
                throw ExpertDslParseException(lineNo, t.message ?: "Parse error")
            }
        }

        return ExpertScriptModel(
            name = name,
            inputs = inputs,
            rules = rules
        )
    }

    private fun parseCondition(expr: String, lineNo: Int): ExpertCondition {
        val e = expr.trim()

        if (e.equals("close > open", ignoreCase = true)) return ExpertCondition.CloseGreaterThanOpen

        if (e.startsWith("profit_gt", ignoreCase = true)) {
            val v = e.substringAfter("profit_gt", "").trim().toDoubleOrNull()
                ?: throw ExpertDslParseException(lineNo, "Invalid profit_gt value")
            return ExpertCondition.ProfitGreaterThan(v)
        }

        if (e.contains(">")) {
            val left = e.substringBefore(">").trim()
            val right = e.substringAfter(">").trim()
            val a = parseEmaPeriod(left, lineNo)
            val b = parseEmaPeriod(right, lineNo)
            return ExpertCondition.EmaGreater(a, b)
        }

        if (e.contains("crosses_above", ignoreCase = true)) {
            val left = e.substringBefore("crosses_above").trim()
            val right = e.substringAfter("crosses_above").trim()
            val a = parseEmaPeriod(left, lineNo)
            val b = parseEmaPeriod(right, lineNo)
            return ExpertCondition.EmaCrossAbove(a, b)
        }

        if (e.contains("crosses_below", ignoreCase = true)) {
            val left = e.substringBefore("crosses_below").trim()
            val right = e.substringAfter("crosses_below").trim()
            val a = parseEmaPeriod(left, lineNo)
            val b = parseEmaPeriod(right, lineNo)
            return ExpertCondition.EmaCrossBelow(a, b)
        }

        throw ExpertDslParseException(lineNo, "Unsupported condition: '$expr'")
    }

    private fun parseEmaPeriod(token: String, lineNo: Int): Int {
        val t = token.trim()
        if (!t.startsWith("ema(", ignoreCase = true) || !t.endsWith(")")) {
            throw ExpertDslParseException(lineNo, "Expected ema(N) but got '$token'")
        }
        val inside = t.substringAfter("(").substringBefore(")").trim()
        val n = inside.toIntOrNull()
            ?: throw ExpertDslParseException(lineNo, "Invalid EMA period in '$token'")
        if (n <= 0) throw ExpertDslParseException(lineNo, "EMA period must be > 0")
        return n
    }
}
