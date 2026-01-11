package com.tradingapp.metatrader.app.features.backtest.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class BacktestPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> BacktestResultsFragment()
            1 -> BacktestGraphFragment()
            2 -> BacktestChartFragment()
            3 -> BacktestJournalFragment()
            else -> BacktestJournalFragment()
        }
    }
}
