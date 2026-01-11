package com.tradingapp.metatrader.domain.repository

import com.tradingapp.metatrader.domain.models.Timeframe
import com.tradingapp.metatrader.domain.models.drawing.DrawingObject
import kotlinx.coroutines.flow.Flow

interface DrawingRepository {
    fun observe(instrument: String, timeframe: Timeframe): Flow<List<DrawingObject>>
    suspend fun replaceAll(instrument: String, timeframe: Timeframe, objects: List<DrawingObject>)
    suspend fun clear(instrument: String, timeframe: Timeframe)
}
