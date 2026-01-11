package com.tradingapp.metatrader.app.di

import com.tradingapp.metatrader.domain.repository.MarketRepository
import com.tradingapp.metatrader.domain.repository.TradingRepository
import com.tradingapp.metatrader.domain.repository.WatchlistRepository
import com.tradingapp.metatrader.domain.usecases.market.GetHistoricalCandlesUseCase
import com.tradingapp.metatrader.domain.usecases.market.StreamTicksUseCase
import com.tradingapp.metatrader.domain.usecases.trading.CancelPendingOrderUseCase
import com.tradingapp.metatrader.domain.usecases.trading.ClosePositionUseCase
import com.tradingapp.metatrader.domain.usecases.trading.ModifyPendingOrderUseCase
import com.tradingapp.metatrader.domain.usecases.trading.ModifyPositionUseCase
import com.tradingapp.metatrader.domain.usecases.trading.ObserveAccountUseCase
import com.tradingapp.metatrader.domain.usecases.trading.ObserveHistoryUseCase
import com.tradingapp.metatrader.domain.usecases.trading.ObservePendingOrdersUseCase
import com.tradingapp.metatrader.domain.usecases.trading.ObservePositionsUseCase
import com.tradingapp.metatrader.domain.usecases.trading.ObserveTradingEventsUseCase
import com.tradingapp.metatrader.domain.usecases.trading.PlaceMarketOrderUseCase
import com.tradingapp.metatrader.domain.usecases.trading.PlacePendingOrderUseCase
import com.tradingapp.metatrader.domain.usecases.watchlist.AddWatchlistItemUseCase
import com.tradingapp.metatrader.domain.usecases.watchlist.ObserveWatchlistUseCase
import com.tradingapp.metatrader.domain.usecases.watchlist.RemoveWatchlistItemUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides @Singleton
    fun provideGetHistoricalCandles(repo: MarketRepository) = GetHistoricalCandlesUseCase(repo)

    @Provides @Singleton
    fun provideStreamTicks(repo: MarketRepository) = StreamTicksUseCase(repo)

    @Provides @Singleton
    fun provideObserveAccount(repo: TradingRepository) = ObserveAccountUseCase(repo)

    @Provides @Singleton
    fun provideObservePositions(repo: TradingRepository) = ObservePositionsUseCase(repo)

    @Provides @Singleton
    fun provideObserveHistory(repo: TradingRepository) = ObserveHistoryUseCase(repo)

    @Provides @Singleton
    fun provideObservePending(repo: TradingRepository) = ObservePendingOrdersUseCase(repo)

    @Provides @Singleton
    fun provideObserveTradingEvents(repo: TradingRepository) = ObserveTradingEventsUseCase(repo)

    @Provides @Singleton
    fun providePlaceMarket(repo: TradingRepository) = PlaceMarketOrderUseCase(repo)

    @Provides @Singleton
    fun providePlacePending(repo: TradingRepository) = PlacePendingOrderUseCase(repo)

    @Provides @Singleton
    fun provideCancelPending(repo: TradingRepository) = CancelPendingOrderUseCase(repo)

    @Provides @Singleton
    fun provideModifyPosition(repo: TradingRepository) = ModifyPositionUseCase(repo)

    @Provides @Singleton
    fun provideModifyPending(repo: TradingRepository) = ModifyPendingOrderUseCase(repo)

    @Provides @Singleton
    fun provideClosePosition(repo: TradingRepository) = ClosePositionUseCase(repo)

    @Provides @Singleton
    fun provideObserveWatchlist(repo: WatchlistRepository) = ObserveWatchlistUseCase(repo)

    @Provides @Singleton
    fun provideAddWatchlist(repo: WatchlistRepository) = AddWatchlistItemUseCase(repo)

    @Provides @Singleton
    fun provideRemoveWatchlist(repo: WatchlistRepository) = RemoveWatchlistItemUseCase(repo)
}
