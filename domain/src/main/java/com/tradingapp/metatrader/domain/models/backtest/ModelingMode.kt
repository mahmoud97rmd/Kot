package com.tradingapp.metatrader.domain.models.backtest

/**
 * MT5-like modeling:
 * - OPEN_PRICES_ONLY: orders fill at next candle OPEN, SL/TP checked only on OPEN (fast, simplified).
 * - CANDLE_EXTREMES: uses candle HIGH/LOW for SL/TP hits within the candle (closer to "every tick" approximation).
 */
enum class ModelingMode {
    OPEN_PRICES_ONLY,
    CANDLE_EXTREMES
}
