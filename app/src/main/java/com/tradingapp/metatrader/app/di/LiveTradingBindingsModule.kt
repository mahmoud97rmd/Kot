package com.tradingapp.metatrader.app.di

import com.tradingapp.metatrader.app.core.market.feed.MarketFeed
import com.tradingapp.metatrader.app.core.trading.TradeExecutor
import com.tradingapp.metatrader.app.features.oanda.streaming.OandaPricingStreamFeed
import com.tradingapp.metatrader.app.features.oanda.trading.OandaTradeExecutor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LiveTradingBindingsModule {

    @Binds
    @Singleton
    abstract fun bindMarketFeed(impl: OandaPricingStreamFeed): MarketFeed

    @Binds
    @Singleton
    abstract fun bindTradeExecutor(impl: OandaTradeExecutor): TradeExecutor
}
