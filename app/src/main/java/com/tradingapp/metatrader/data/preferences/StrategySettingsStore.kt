package com.tradingapp.metatrader.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tradingapp.metatrader.domain.models.strategy.StrategySettings
import com.tradingapp.metatrader.domain.repository.StrategySettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.strategyDataStore by preferencesDataStore(name = "strategy_settings")

class StrategySettingsStore(
    private val context: Context
) : StrategySettingsRepository {

    private object Keys {
        val RISK = doublePreferencesKey("riskPercent")
        val ATR_PERIOD = intPreferencesKey("atrPeriod")
        val SL_ATR = doublePreferencesKey("slAtrMult")
        val TP_ATR = doublePreferencesKey("tpAtrMult")
        val EMA_FAST = intPreferencesKey("emaFast")
        val EMA_SLOW = intPreferencesKey("emaSlow")
        val STOCH_PERIOD = intPreferencesKey("stochPeriod")
        val STOCH_TRIGGER = doublePreferencesKey("stochTrigger")
    }

    override fun observe(): Flow<StrategySettings> {
        return context.strategyDataStore.data.map { p ->
            StrategySettings(
                riskPercent = p[Keys.RISK] ?: 1.0,
                atrPeriod = p[Keys.ATR_PERIOD] ?: 14,
                slAtrMult = p[Keys.SL_ATR] ?: 1.5,
                tpAtrMult = p[Keys.TP_ATR] ?: 2.0,
                emaFast = p[Keys.EMA_FAST] ?: 50,
                emaSlow = p[Keys.EMA_SLOW] ?: 150,
                stochPeriod = p[Keys.STOCH_PERIOD] ?: 14,
                stochTrigger = p[Keys.STOCH_TRIGGER] ?: 20.0
            )
        }
    }

    override suspend fun update(transform: (StrategySettings) -> StrategySettings) {
        context.strategyDataStore.edit { p ->
            val cur = StrategySettings(
                riskPercent = p[Keys.RISK] ?: 1.0,
                atrPeriod = p[Keys.ATR_PERIOD] ?: 14,
                slAtrMult = p[Keys.SL_ATR] ?: 1.5,
                tpAtrMult = p[Keys.TP_ATR] ?: 2.0,
                emaFast = p[Keys.EMA_FAST] ?: 50,
                emaSlow = p[Keys.EMA_SLOW] ?: 150,
                stochPeriod = p[Keys.STOCH_PERIOD] ?: 14,
                stochTrigger = p[Keys.STOCH_TRIGGER] ?: 20.0
            )
            val next = transform(cur)
            p[Keys.RISK] = next.riskPercent
            p[Keys.ATR_PERIOD] = next.atrPeriod
            p[Keys.SL_ATR] = next.slAtrMult
            p[Keys.TP_ATR] = next.tpAtrMult
            p[Keys.EMA_FAST] = next.emaFast
            p[Keys.EMA_SLOW] = next.emaSlow
            p[Keys.STOCH_PERIOD] = next.stochPeriod
            p[Keys.STOCH_TRIGGER] = next.stochTrigger
        }
    }

    override suspend fun set(settings: StrategySettings) {
        context.strategyDataStore.edit { p ->
            p[Keys.RISK] = settings.riskPercent
            p[Keys.ATR_PERIOD] = settings.atrPeriod
            p[Keys.SL_ATR] = settings.slAtrMult
            p[Keys.TP_ATR] = settings.tpAtrMult
            p[Keys.EMA_FAST] = settings.emaFast
            p[Keys.EMA_SLOW] = settings.emaSlow
            p[Keys.STOCH_PERIOD] = settings.stochPeriod
            p[Keys.STOCH_TRIGGER] = settings.stochTrigger
        }
    }
}
