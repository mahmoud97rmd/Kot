package com.tradingapp.metatrader.data.repository

import com.tradingapp.metatrader.data.remote.stream.OandaPricingStreamClient
import com.tradingapp.metatrader.domain.models.Tick
import com.tradingapp.metatrader.domain.repository.PriceRepository
import kotlinx.coroutines.flow.Flow

class PriceRepositoryImpl(
    private val streamClient: OandaPricingStreamClient
) : PriceRepository {

    override fun streamPrices(instruments: List<String>): Flow<Tick> {
        val csv = instruments.distinct().joinToString(",")
        return streamClient.streamTicks(csv)
    }
}
