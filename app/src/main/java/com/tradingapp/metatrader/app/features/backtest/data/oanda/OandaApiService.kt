package com.tradingapp.metatrader.app.features.backtest.data.oanda

import com.tradingapp.metatrader.app.features.backtest.data.oanda.dto.OandaCandlesResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OandaApiService {

    @GET("v3/instruments/{instrument}/candles")
    suspend fun getCandles(
        @Path("instrument") instrument: String,
        @Query("granularity") granularity: String,
        @Query("price") price: String = "M",
        @Query("from") fromIso: String? = null,
        @Query("to") toIso: String? = null,
        @Query("count") count: Int? = null
    ): OandaCandlesResponse
}
