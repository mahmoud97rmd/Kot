package com.tradingapp.metatrader.app.features.backtest.expert

import com.tradingapp.metatrader.app.features.expert.engine.backtest.ExpertBacktestRunner
import com.tradingapp.metatrader.app.features.expert.engine.shared.AttachedExpertResolver
import com.tradingapp.metatrader.app.features.expert.engine.shared.ExpertCodeProvider
import com.tradingapp.metatrader.domain.models.backtest.BacktestCandle
import com.tradingapp.metatrader.domain.models.backtest.BacktestConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RunAttachedExpertBacktestUseCase @Inject constructor(
    private val resolver: AttachedExpertResolver,
    private val codeProvider: ExpertCodeProvider,
    private val runner: ExpertBacktestRunner
) {
    data class Output(
        val ok: Boolean,
        val message: String,
        val result: com.tradingapp.metatrader.domain.models.backtest.BacktestResult? = null,
        val logs: List<String> = emptyList()
    )

    suspend fun run(
        symbol: String,
        timeframe: String,
        candles: List<BacktestCandle>,
        config: BacktestConfig
    ): Output {
        val scriptId = resolver.resolveScriptId(symbol, timeframe)
            ?: return Output(false, "No EA attached to $symbol $timeframe")

        val composedCode = codeProvider.getComposedCode(scriptId)
        val out = runner.run(
            candles = candles,
            expertCode = composedCode,
            symbol = symbol,
            timeframe = timeframe,
            config = config
        )
        return Output(true, "EA backtest OK (attached EA)", out.result, out.logs)
    }
}
