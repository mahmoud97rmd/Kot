package com.tradingapp.metatrader.app.features.oanda.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.oandaDataStore by preferencesDataStore(name = "oanda_settings")

@Singleton
class OandaSettingsStore @Inject constructor(
    @ApplicationContext private val ctx: Context
) {
    private val KEY_TOKEN = stringPreferencesKey("token")
    private val KEY_ACCOUNT_ID = stringPreferencesKey("account_id")
    private val KEY_ENV = stringPreferencesKey("env") // "practice" or "live"

    data class OandaSettings(
        val token: String,
        val accountId: String,
        val env: String
    )

    val settingsFlow: Flow<OandaSettings> =
        ctx.oandaDataStore.data.let { ds ->
            val tokenFlow = ds.map { it[KEY_TOKEN] ?: "" }
            val accFlow = ds.map { it[KEY_ACCOUNT_ID] ?: "" }
            val envFlow = ds.map { it[KEY_ENV] ?: "practice" }
            combine(tokenFlow, accFlow, envFlow) { t, a, e -> OandaSettings(t, a, e) }
        }

    suspend fun setToken(token: String) {
        ctx.oandaDataStore.edit { it[KEY_TOKEN] = token.trim() }
    }

    suspend fun setAccountId(accountId: String) {
        ctx.oandaDataStore.edit { it[KEY_ACCOUNT_ID] = accountId.trim() }
    }

    suspend fun setEnv(env: String) {
        ctx.oandaDataStore.edit { it[KEY_ENV] = env.trim() }
    }
}
