package com.tradingapp.metatrader.app.features.expert.templates

object DefaultExpertTemplates {

    val demoTradeJs: String = """
        function onInit(api) {
          api.log("INFO", "EA loaded: " + EA_NAME + " on " + SYMBOL + " " + TIMEFRAME);
        }

        function onTick(tick, api) {
          // optional: tick-based logic
        }

        function onBar(bar, api) {
          // very simple demo logic:
          // if bullish bar -> buy
          // if bearish bar -> sell
          if (bar.close > bar.open) {
            api.log("INFO", "Bullish bar => BUY");
            api.buy(1000, null, null);
          } else if (bar.close < bar.open) {
            api.log("INFO", "Bearish bar => SELL");
            api.sell(1000, null, null);
          }
        }
    """.trimIndent()
}
