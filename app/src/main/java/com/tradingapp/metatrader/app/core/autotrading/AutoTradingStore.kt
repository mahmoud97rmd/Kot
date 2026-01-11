package com.tradingapp.metatrader.app.core.autotrading

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.autoTradingDataStore by preferencesDataStore(name = "auto_trading")

@Singleton
class AutoTradingStore @Inject constructor(
    @ApplicationContext private val ctx: Context
) {
    private val KEY = booleanPreferencesKey("enabled")

    val enabledFlow: Flow<Boolean> =
        ctx.autoTradingDataStore.data.map { prefs -> prefs[KEY] ?: false }

    suspend fun setEnabled(enabled: Boolean) {
        ctx.autoTradingDataStore.edit { prefs -> prefs[KEY] = enabled }
    }

    suspend fun toggle() {
        val cur = enabledFlow.first()
        setEnabled(!cur)
    }

    suspend fun isEnabledNow(): Boolean = enabledFlow.first()
}
