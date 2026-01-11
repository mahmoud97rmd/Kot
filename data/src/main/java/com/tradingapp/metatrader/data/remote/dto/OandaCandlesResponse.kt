package com.tradingapp.metatrader.data.remote.dto

import com.squareup.moshi.Json

data class OandaCandlesResponse(
    @Json(name = "candles") val candles: List<OandaCandleDto>
)

data class OandaCandleDto(
    @Json(name = "complete") val complete: Boolean,
    @Json(name = "time") val time: String,
    @Json(name = "volume") val volume: Long?,
    @Json(name = "mid") val mid: OandaCandleMidDto?
)

data class OandaCandleMidDto(
    @Json(name = "o") val o: String,
    @Json(name = "h") val h: String,
    @Json(name = "l") val l: String,
    @Json(name = "c") val c: String
)
