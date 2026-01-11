package com.tradingapp.metatrader.app.di

import com.tradingapp.metatrader.core.engine.trading.TradingEngine
import com.tradingapp.metatrader.data.local.database.dao.CandleDao
import com.tradingapp.metatrader.data.local.database.dao.ClosedTradeDao
import com.tradingapp.metatrader.data.local.database.dao.PendingOrderDao
import com.tradingapp.metatrader.data.local.database.dao.PositionDao
import com.tradingapp.metatrader.data.local.database.dao.WatchlistDao
import com.tradingapp.metatrader.data.remote.api.OandaApiService
import com.tradingapp.metatrader.data.remote.stream.OandaPricingStreamClient
import com.tradingapp.metatrader.data.repository.MarketRepositoryImpl
import com.tradingapp.metatrader.data.repository.PriceRepositoryImpl
import com.tradingapp.metatrader.data.repository.TradingRepositoryImpl
import com.tradingapp.metatrader.data.repository.WatchlistRepositoryImpl
import com.tradingapp.metatrader.domain.repository.MarketRepository
import com.tradingapp.metatrader.domain.repository.PriceRepository
import com.tradingapp.metatrader.domain.repository.TradingEngineInput
import com.tradingapp.metatrader.domain.repository.TradingRepository
import com.tradingapp.metatrader.domain.repository.WatchlistRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides @Singleton
    fun provideMarketRepo(
        api: OandaApiService,
        candleDao: CandleDao,
        stream: OandaPricingStreamClient
    ): MarketRepository = MarketRepositoryImpl(api, candleDao, stream)

    @Provides @Singleton
    fun provideTradingRepoImpl(
        engine: TradingEngine,
        positionDao: PositionDao,
        closedDao: ClosedTradeDao,
        pendingDao: PendingOrderDao
    ): TradingRepositoryImpl = TradingRepositoryImpl(engine, positionDao, closedDao, pendingDao)

    @Provides @Singleton
    fun provideTradingRepo(impl: TradingRepositoryImpl): TradingRepository = impl

    @Provides @Singleton
    fun provideTradingEngineInput(impl: TradingRepositoryImpl): TradingEngineInput = impl

    @Provides @Singleton
    fun provideWatchlistRepo(watchlistDao: WatchlistDao): WatchlistRepository =
        WatchlistRepositoryImpl(watchlistDao)

    @Provides @Singleton
    fun providePriceRepo(stream: OandaPricingStreamClient): PriceRepository =
        PriceRepositoryImpl(stream)
}
