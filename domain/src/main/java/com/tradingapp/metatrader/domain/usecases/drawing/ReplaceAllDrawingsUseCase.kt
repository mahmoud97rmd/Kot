package com.tradingapp.metatrader.domain.usecases.drawing

import com.tradingapp.metatrader.domain.models.Timeframe
import com.tradingapp.metatrader.domain.models.drawing.DrawingObject
import com.tradingapp.metatrader.domain.repository.DrawingRepository
import javax.inject.Inject

class ReplaceAllDrawingsUseCase @Inject constructor(
    private val repo: DrawingRepository
) {
    suspend operator fun invoke(instrument: String, timeframe: Timeframe, objects: List<DrawingObject>) {
        repo.replaceAll(instrument, timeframe, objects)
    }
}
