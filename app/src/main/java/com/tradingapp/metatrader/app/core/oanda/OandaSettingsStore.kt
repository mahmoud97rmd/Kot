package com.tradingapp.metatrader.app.core.oanda

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.oandaDataStore by preferencesDataStore(name = "oanda_settings")

@Singleton
class OandaSettingsStore @Inject constructor(
    @ApplicationContext private val ctx: Context
) {
    private val KEY_TOKEN = stringPreferencesKey("token")
    private val KEY_ACCOUNT = stringPreferencesKey("account_id")
    private val KEY_ENV = stringPreferencesKey("env") // PRACTICE/LIVE

    val flow: Flow<OandaSettings?> = ctx.oandaDataStore.data.map { prefs ->
        val token = (prefs[KEY_TOKEN] ?: "").trim()
        val acc = (prefs[KEY_ACCOUNT] ?: "").trim()
        val envRaw = (prefs[KEY_ENV] ?: "PRACTICE").trim().uppercase()

        if (token.isBlank() || acc.isBlank()) return@map null

        val env = runCatching { OandaEnvironment.valueOf(envRaw) }
            .getOrElse { OandaEnvironment.PRACTICE }

        OandaSettings(apiToken = token, accountId = acc, environment = env)
    }

    suspend fun getNow(): OandaSettings? = flow.first()

    suspend fun setToken(token: String) {
        ctx.oandaDataStore.edit { it[KEY_TOKEN] = token.trim() }
    }

    suspend fun setAccountId(id: String) {
        ctx.oandaDataStore.edit { it[KEY_ACCOUNT] = id.trim() }
    }

    suspend fun setEnvironment(env: OandaEnvironment) {
        ctx.oandaDataStore.edit { it[KEY_ENV] = env.name }
    }
}
