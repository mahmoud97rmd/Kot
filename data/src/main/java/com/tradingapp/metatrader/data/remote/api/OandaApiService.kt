package com.tradingapp.metatrader.data.remote.api

import com.tradingapp.metatrader.data.remote.dto.OandaCandlesResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OandaApiService {

    @GET("v3/instruments/{instrument}/candles")
    suspend fun getCandles(
        @Path("instrument") instrument: String,
        @Query("granularity") granularity: String,
        @Query("count") count: Int = 500,
        @Query("price") price: String = "M" // Mid
    ): OandaCandlesResponse
}
