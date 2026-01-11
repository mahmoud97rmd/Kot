package com.tradingapp.metatrader.app.features.oanda.trading

import com.tradingapp.metatrader.app.core.trading.MarketOrderRequest
import com.tradingapp.metatrader.app.core.trading.OrderResult
import com.tradingapp.metatrader.app.core.trading.OrderSide
import com.tradingapp.metatrader.app.core.trading.TradeExecutor
import com.tradingapp.metatrader.app.features.oanda.net.OandaEndpoints
import com.tradingapp.metatrader.app.features.oanda.settings.OandaSettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OandaTradeExecutor @Inject constructor(
    private val okHttp: OkHttpClient,
    private val settingsStore: OandaSettingsStore
) : TradeExecutor {

    override suspend fun placeMarketOrder(req: MarketOrderRequest): OrderResult = withContext(Dispatchers.IO) {
        val s = settingsStore.settingsFlow.first()
        if (s.token.isBlank() || s.accountId.isBlank()) {
            return@withContext OrderResult(false, "Missing OANDA token/accountId. Configure settings first.")
        }

        val base = OandaEndpoints.restBase(s.env)
        val url = "$base/v3/accounts/${s.accountId}/orders"

        val signedUnits = when (req.side) {
            OrderSide.BUY -> kotlin.math.abs(req.units)
            OrderSide.SELL -> -kotlin.math.abs(req.units)
        }

        // Minimal OANDA market order JSON.
        // Endpoint: POST /v3/accounts/{accountID}/orders :contentReference[oaicite:7]{index=7}
        val order = JSONObject().apply {
            put("type", "MARKET")
            put("instrument", req.symbol)
            put("units", signedUnits.toString())
            put("timeInForce", "FOK")
            put("positionFill", "DEFAULT")

            req.takeProfitPrice?.let { tp ->
                put("takeProfitOnFill", JSONObject().put("price", tp.toString()))
            }
            req.stopLossPrice?.let { sl ->
                put("stopLossOnFill", JSONObject().put("price", sl.toString()))
            }
        }

        val payload = JSONObject().put("order", order).toString()
        val body = payload.toRequestBody("application/json".toMediaType())

        val httpReq = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${s.token}")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        val resp = okHttp.newCall(httpReq).execute()
        val raw = resp.body?.string()

        if (!resp.isSuccessful) {
            return@withContext OrderResult(false, "OANDA order failed HTTP ${resp.code}", raw)
        }

        OrderResult(true, "Order placed", raw)
    }
}
