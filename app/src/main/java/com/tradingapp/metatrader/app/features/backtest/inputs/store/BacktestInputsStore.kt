package com.tradingapp.metatrader.app.features.backtest.inputs.store

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tradingapp.metatrader.app.datastore.backtestDataStore
import com.tradingapp.metatrader.app.features.backtest.inputs.BacktestInputs
import com.tradingapp.metatrader.app.features.backtest.strategy.StrategyType
import com.tradingapp.metatrader.domain.models.backtest.ModelingMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BacktestInputsStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val STRATEGY = stringPreferencesKey("strategy_type")
        val MODELING = stringPreferencesKey("modeling_mode")

        val EMA_FAST = intPreferencesKey("ema_fast")
        val EMA_SLOW = intPreferencesKey("ema_slow")

        val RSI_PERIOD = intPreferencesKey("rsi_period")
        val RSI_OS = doublePreferencesKey("rsi_oversold")
        val RSI_OB = doublePreferencesKey("rsi_overbought")

        val STOCH_K = intPreferencesKey("stoch_k")
        val STOCH_D = intPreferencesKey("stoch_d")
        val STOCH_OS = doublePreferencesKey("stoch_oversold")
        val STOCH_OB = doublePreferencesKey("stoch_overbought")

        // new: risk/levels
        val SL_POINTS = doublePreferencesKey("stop_loss_points")
        val TP_POINTS = doublePreferencesKey("take_profit_points")
        val RISK_PCT = doublePreferencesKey("risk_percent")

        val LOTS = doublePreferencesKey("lots")

        val INITIAL_BAL = doublePreferencesKey("initial_balance")
        val COMMISSION = doublePreferencesKey("commission_per_lot")
        val SPREAD = doublePreferencesKey("spread_points")
        val SLIPPAGE = doublePreferencesKey("slippage_points")
        val POINT_VALUE = doublePreferencesKey("point_value")
    }

    val inputsFlow: Flow<BacktestInputs> = context.backtestDataStore.data.map { prefs ->
        val st = prefs[Keys.STRATEGY]
        val strategyType = runCatching { StrategyType.valueOf(st ?: StrategyType.EMA_CROSS.name) }
            .getOrElse { StrategyType.EMA_CROSS }

        val mm = prefs[Keys.MODELING]
        val modelingMode = runCatching { ModelingMode.valueOf(mm ?: ModelingMode.CANDLE_EXTREMES.name) }
            .getOrElse { ModelingMode.CANDLE_EXTREMES }

        BacktestInputs(
            strategyType = strategyType,
            modelingMode = modelingMode,

            emaFast = prefs[Keys.EMA_FAST] ?: 10,
            emaSlow = prefs[Keys.EMA_SLOW] ?: 30,

            rsiPeriod = prefs[Keys.RSI_PERIOD] ?: 14,
            rsiOversold = prefs[Keys.RSI_OS] ?: 30.0,
            rsiOverbought = prefs[Keys.RSI_OB] ?: 70.0,

            stochK = prefs[Keys.STOCH_K] ?: 14,
            stochD = prefs[Keys.STOCH_D] ?: 3,
            stochOversold = prefs[Keys.STOCH_OS] ?: 20.0,
            stochOverbought = prefs[Keys.STOCH_OB] ?: 80.0,

            stopLossPoints = prefs[Keys.SL_POINTS] ?: 0.0,
            takeProfitPoints = prefs[Keys.TP_POINTS] ?: 0.0,
            riskPercent = prefs[Keys.RISK_PCT] ?: 0.0,

            lots = prefs[Keys.LOTS] ?: 1.0,

            initialBalance = prefs[Keys.INITIAL_BAL] ?: 10_000.0,
            commissionPerLot = prefs[Keys.COMMISSION] ?: 0.0,
            spreadPoints = prefs[Keys.SPREAD] ?: 2.0,
            slippagePoints = prefs[Keys.SLIPPAGE] ?: 0.5,
            pointValue = prefs[Keys.POINT_VALUE] ?: 0.01
        )
    }

    suspend fun save(inputs: BacktestInputs) {
        context.backtestDataStore.edit { prefs ->
            prefs[Keys.STRATEGY] = inputs.strategyType.name
            prefs[Keys.MODELING] = inputs.modelingMode.name

            prefs[Keys.EMA_FAST] = inputs.emaFast
            prefs[Keys.EMA_SLOW] = inputs.emaSlow

            prefs[Keys.RSI_PERIOD] = inputs.rsiPeriod
            prefs[Keys.RSI_OS] = inputs.rsiOversold
            prefs[Keys.RSI_OB] = inputs.rsiOverbought

            prefs[Keys.STOCH_K] = inputs.stochK
            prefs[Keys.STOCH_D] = inputs.stochD
            prefs[Keys.STOCH_OS] = inputs.stochOversold
            prefs[Keys.STOCH_OB] = inputs.stochOverbought

            prefs[Keys.SL_POINTS] = inputs.stopLossPoints
            prefs[Keys.TP_POINTS] = inputs.takeProfitPoints
            prefs[Keys.RISK_PCT] = inputs.riskPercent

            prefs[Keys.LOTS] = inputs.lots

            prefs[Keys.INITIAL_BAL] = inputs.initialBalance
            prefs[Keys.COMMISSION] = inputs.commissionPerLot
            prefs[Keys.SPREAD] = inputs.spreadPoints
            prefs[Keys.SLIPPAGE] = inputs.slippagePoints
            prefs[Keys.POINT_VALUE] = inputs.pointValue
        }
    }
}
