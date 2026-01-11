package com.tradingapp.metatrader.app.features.expert.inputs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.expertInputsDataStore by preferencesDataStore(name = "expert_inputs")

@Singleton
class ExpertInputsStore @Inject constructor(
    @ApplicationContext private val ctx: Context
) {

    private fun keyFor(scriptId: String) = stringPreferencesKey("inputs_$scriptId")

    fun inputsJsonFlow(scriptId: String): Flow<String> =
        ctx.expertInputsDataStore.data.map { prefs ->
            prefs[keyFor(scriptId)] ?: "{}"
        }

    suspend fun getInputsJson(scriptId: String): String {
        // Use flow with first() to avoid blocking preference read.
        return inputsJsonFlow(scriptId).kotlinx.coroutines.flow.first()
    }

    suspend fun setInputsJson(scriptId: String, json: String) {
        val value = json.trim().ifBlank { "{}" }
        ctx.expertInputsDataStore.edit { prefs ->
            prefs[keyFor(scriptId)] = value
        }
    }
}
