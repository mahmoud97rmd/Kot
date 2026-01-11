package com.tradingapp.metatrader.app.features.backtest.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.tradingapp.metatrader.app.R
import com.tradingapp.metatrader.app.features.backtest.BacktestViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class BacktestResultsFragment : Fragment(android.R.layout.simple_list_item_1) {

    private val vm: BacktestViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tv: TextView = view.findViewById(android.R.id.text1)
        tv.setTextColor(0xFFD1D4DC.toInt())
        tv.setBackgroundColor(0xFF0B1220.toInt())

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.state.collectLatest { st ->
                val r = st.result
                tv.text = if (r == null) {
                    "No results yet."
                } else {
                    "Trades=${r.totalTrades}\nWinRate=${"%.2f".format(r.winRate * 100)}%\nNetProfit=${"%.2f".format(r.netProfit)}\nMaxDD=${"%.2f".format(r.maxDrawdown)}"
                }
            }
        }
    }
}
