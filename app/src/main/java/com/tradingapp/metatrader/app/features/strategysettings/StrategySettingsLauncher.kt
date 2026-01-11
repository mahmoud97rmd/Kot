package com.tradingapp.metatrader.app.features.strategysettings

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

object StrategySettingsLauncher {
    fun show(fm: FragmentManager) {
        StrategySettingsBottomSheet.newInstance().show(fm, "strategy_settings")
    }
}
