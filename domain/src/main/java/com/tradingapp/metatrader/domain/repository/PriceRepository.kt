package com.tradingapp.metatrader.domain.repository

import com.tradingapp.metatrader.domain.models.Tick
import kotlinx.coroutines.flow.Flow

/**
 * يبث تيكات الأسعار لعدة instruments عبر stream واحد.
 * OANDA يسمح بتمرير instruments=AAA,BBB,CCC
 */
interface PriceRepository {
    fun streamPrices(instruments: List<String>): Flow<Tick>
}
