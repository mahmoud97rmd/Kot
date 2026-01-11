package com.tradingapp.metatrader.app.core.expert.dsl

data class ExpertScriptModel(
    val name: String,
    val inputs: Map<String, Double>,
    val rules: List<ExpertRule>
)

data class ExpertRule(
    val action: ExpertAction,
    val condition: ExpertCondition
)

enum class ExpertAction {
    BUY,
    SELL,
    CLOSE_ALL
}

sealed class ExpertCondition {
    data class EmaGreater(val a: Int, val b: Int) : ExpertCondition()
    data class EmaCrossAbove(val a: Int, val b: Int) : ExpertCondition()
    data class EmaCrossBelow(val a: Int, val b: Int) : ExpertCondition()
    object CloseGreaterThanOpen : ExpertCondition()
    data class ProfitGreaterThan(val amount: Double) : ExpertCondition()
}
