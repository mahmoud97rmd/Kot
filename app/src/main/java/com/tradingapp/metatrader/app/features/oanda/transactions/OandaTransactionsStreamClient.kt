package com.tradingapp.metatrader.app.features.oanda.transactions

import com.tradingapp.metatrader.app.core.journal.LiveJournalBus
import com.tradingapp.metatrader.app.features.oanda.net.OandaEndpoints
import com.tradingapp.metatrader.app.features.oanda.settings.OandaSettingsStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OandaTransactionsStreamClient @Inject constructor(
    private val okHttp: OkHttpClient,
    private val settingsStore: OandaSettingsStore,
    private val journal: LiveJournalBus
) {
    @Volatile private var running: Boolean = false

    suspend fun runStreamLoop() = withContext(Dispatchers.IO) {
        if (running) return@withContext
        running = true

        try {
            val s = settingsStore.settingsFlow.first()
            if (s.token.isBlank() || s.accountId.isBlank()) {
                journal.post("OANDA", "ERROR", "Transactions stream: missing token/accountId.")
                return@withContext
            }

            val base = OandaEndpoints.pricingStreamBase(s.env) // stream host (same family)
            // Endpoint: /v3/accounts/{accountID}/transactions/stream :contentReference[oaicite:2]{index=2}
            val url = "$base/v3/accounts/${s.accountId}/transactions/stream"

            val req = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer ${s.token}")
                .get()
                .build()

            journal.post("OANDA", "INFO", "Transactions stream connecting...")

            val call = okHttp.newCall(req)
            val resp = call.execute()

            if (!resp.isSuccessful) {
                journal.post("OANDA", "ERROR", "Transactions stream HTTP ${resp.code} ${resp.message}")
                return@withContext
            }

            val body = resp.body ?: run {
                journal.post("OANDA", "ERROR", "Transactions stream: empty body")
                return@withContext
            }

            BufferedReader(InputStreamReader(body.byteStream())).use { reader ->
                journal.post("OANDA", "INFO", "Transactions stream connected.")
                while (running) {
                    val line = reader.readLine() ?: break
                    if (line.isBlank()) continue

                    val obj = runCatching { JSONObject(line) }.getOrNull() ?: continue

                    // Stream sends: "transaction" or "heartbeat" (depending on server)
                    val type = obj.optString("type", "")
                    if (type.equals("HEARTBEAT", true)) {
                        continue
                    }

                    // Sometimes payload is nested under "transaction"
                    val txn = obj.optJSONObject("transaction") ?: obj
                    val txnType = txn.optString("type", type.ifBlank { "TRANSACTION" })
                    val id = txn.optString("id", "")
                    val instrument = txn.optString("instrument", "")
                    val reason = txn.optString("reason", "")

                    val msg = buildString {
                        append(txnType)
                        if (id.isNotBlank()) append(" #").append(id)
                        if (instrument.isNotBlank()) append(" ").append(instrument)
                        if (reason.isNotBlank()) append(" reason=").append(reason)
                    }

                    journal.post("OANDA", "INFO", msg)
                }
            }
        } catch (ce: CancellationException) {
            // ignore
        } catch (e: Exception) {
            journal.post("OANDA", "ERROR", "Transactions stream error: ${e.message ?: "unknown"}")
        } finally {
            running = false
            journal.post("OANDA", "WARN", "Transactions stream disconnected.")
        }
    }

    fun stop() {
        running = false
    }
}
