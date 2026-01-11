package com.tradingapp.metatrader.app.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

private const val DS_NAME = "backtest_inputs"

val Context.backtestDataStore by preferencesDataStore(name = DS_NAME)
