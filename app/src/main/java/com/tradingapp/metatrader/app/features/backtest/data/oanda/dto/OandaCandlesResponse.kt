package com.tradingapp.metatrader.app.features.backtest.data.oanda.dto

import com.squareup.moshi.Json

data class OandaCandlesResponse(
    @Json(name = "instrument") val instrument: String?,
    @Json(name = "granularity") val granularity: String?,
    @Json(name = "candles") val candles: List<OandaCandleDto> = emptyList()
)

data class OandaCandleDto(
    @Json(name = "complete") val complete: Boolean = false,
    @Json(name = "time") val time: String,
    @Json(name = "mid") val mid: OandaPriceDto?
)

data class OandaPriceDto(
    @Json(name = "o") val o: String,
    @Json(name = "h") val h: String,
    @Json(name = "l") val l: String,
    @Json(name = "c") val c: String
)
