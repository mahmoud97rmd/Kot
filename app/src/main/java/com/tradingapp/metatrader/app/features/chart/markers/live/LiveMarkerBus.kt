package com.tradingapp.metatrader.app.features.chart.markers.live

import com.tradingapp.metatrader.app.features.chart.markers.ChartMarker
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LiveMarkerBus @Inject constructor() {
    private val _flow = MutableSharedFlow<ChartMarker>(extraBufferCapacity = 256)
    val flow: SharedFlow<ChartMarker> = _flow

    fun post(marker: ChartMarker) {
        _flow.tryEmit(marker)
    }
}
