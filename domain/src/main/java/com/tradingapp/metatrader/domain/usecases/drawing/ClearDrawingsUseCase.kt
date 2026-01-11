package com.tradingapp.metatrader.domain.usecases.drawing

import com.tradingapp.metatrader.domain.models.Timeframe
import com.tradingapp.metatrader.domain.repository.DrawingRepository
import javax.inject.Inject

class ClearDrawingsUseCase @Inject constructor(
    private val repo: DrawingRepository
) {
    suspend operator fun invoke(instrument: String, timeframe: Timeframe) {
        repo.clear(instrument, timeframe)
    }
}
