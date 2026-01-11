package com.tradingapp.metatrader.app.features.strategy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingapp.metatrader.domain.models.Candle
import com.tradingapp.metatrader.domain.models.strategy.StrategySettings
import com.tradingapp.metatrader.domain.models.trading.Position
import com.tradingapp.metatrader.domain.usecases.strategy.ObserveStrategySettingsUseCase
import com.tradingapp.metatrader.domain.usecases.trading.ExecuteMarketOrderUseCase
import com.tradingapp.metatrader.domain.utils.risk.LotCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AutoTradingViewModel @Inject constructor(
    observeSettings: ObserveStrategySettingsUseCase,
    private val executeMarket: ExecuteMarketOrderUseCase
) : ViewModel() {

    private val latestSettings = MutableStateFlow(StrategySettings())

    private var lastProcessedCandleEpoch: Long? = null
    private var prevStochK: Double? = null

    init {
        viewModelScope.launch {
            observeSettings().collectLatest { latestSettings.value = it }
        }
    }

    fun onNewCandleClosed(
        instrument: String,
        closedHistory: List<Candle>,
        lastPriceForMarket: Double,
        balanceForRisk: Double = 10_000.0
    ) {
        val cfg = latestSettings.value

        if (closedHistory.size < 200) return
        val last = closedHistory.last()
        val epoch = last.time.epochSecond
        if (lastProcessedCandleEpoch == epoch) return
        lastProcessedCandleEpoch = epoch

        val closes = closedHistory.map { it.close }
        val emaFast = Indicators.ema(closes, cfg.emaFast) ?: return
        val emaSlow = Indicators.ema(closes, cfg.emaSlow) ?: return

        val k = Indicators.stochasticK(closedHistory, cfg.stochPeriod) ?: return
        val kPrev = prevStochK
        prevStochK = k

        val signalBuy =
            (emaFast > emaSlow) &&
            (kPrev != null && kPrev < cfg.stochTrigger) &&
            (k > cfg.stochTrigger)

        if (!signalBuy) return

        val atr = Atr.atr(closedHistory, cfg.atrPeriod) ?: return

        val entry = lastPriceForMarket
        val sl = entry - (atr * cfg.slAtrMult)
        val tp = entry + (atr * cfg.tpAtrMult)

        val lots = LotCalculator.calcLots(
            instrument = instrument,
            balance = balanceForRisk,
            riskPercent = cfg.riskPercent,
            entryPrice = entry,
            stopLossPrice = sl
        )

        viewModelScope.launch {
            runCatching {
                executeMarket(
                    instrument = instrument,
                    side = Position.Side.BUY,
                    price = entry,
                    lots = lots,
                    stopLoss = sl,
                    takeProfit = tp
                )
            }
        }
    }

    fun reset() {
        lastProcessedCandleEpoch = null
        prevStochK = null
    }
}
