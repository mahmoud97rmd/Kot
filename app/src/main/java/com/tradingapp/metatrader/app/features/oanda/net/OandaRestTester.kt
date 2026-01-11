package com.tradingapp.metatrader.app.features.oanda.net

import com.tradingapp.metatrader.app.features.oanda.settings.OandaSettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OandaRestTester @Inject constructor(
    private val okHttp: OkHttpClient,
    private val settingsStore: OandaSettingsStore
) {

    data class TestResult(
        val ok: Boolean,
        val message: String,
        val raw: String? = null
    )

    suspend fun testAccountSummary(): TestResult = withContext(Dispatchers.IO) {
        val s = settingsStore.settingsFlow.first()
        if (s.token.isBlank() || s.accountId.isBlank()) {
            return@withContext TestResult(false, "Missing token/accountId.")
        }

        val base = OandaEndpoints.restBase(s.env)
        val url = "$base/v3/accounts/${s.accountId}/summary"

        val req = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${s.token}")
            .get()
            .build()

        val resp = okHttp.newCall(req).execute()
        val raw = resp.body?.string()

        if (!resp.isSuccessful) {
            return@withContext TestResult(false, "HTTP ${resp.code} ${resp.message}", raw)
        }
        TestResult(true, "Connection OK (account summary).", raw)
    }

    suspend fun fetchAccounts(): TestResult = withContext(Dispatchers.IO) {
        val s = settingsStore.settingsFlow.first()
        if (s.token.isBlank()) {
            return@withContext TestResult(false, "Missing token.")
        }

        val base = OandaEndpoints.restBase(s.env)
        val url = "$base/v3/accounts"

        val req = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${s.token}")
            .get()
            .build()

        val resp = okHttp.newCall(req).execute()
        val raw = resp.body?.string()

        if (!resp.isSuccessful) {
            return@withContext TestResult(false, "HTTP ${resp.code} ${resp.message}", raw)
        }
        TestResult(true, "Accounts fetched OK.", raw)
    }
}
