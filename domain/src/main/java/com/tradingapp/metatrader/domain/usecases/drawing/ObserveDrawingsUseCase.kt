package com.tradingapp.metatrader.domain.usecases.drawing

import com.tradingapp.metatrader.domain.models.Timeframe
import com.tradingapp.metatrader.domain.models.drawing.DrawingObject
import com.tradingapp.metatrader.domain.repository.DrawingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveDrawingsUseCase @Inject constructor(
    private val repo: DrawingRepository
) {
    operator fun invoke(instrument: String, timeframe: Timeframe): Flow<List<DrawingObject>> =
        repo.observe(instrument, timeframe)
}
