package com.tradingapp.metatrader.app.features.backtest.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.tradingapp.metatrader.app.R
import com.tradingapp.metatrader.app.features.backtest.BacktestViewModel
import com.tradingapp.metatrader.app.features.backtest.equity.EquityWebView
import kotlinx.coroutines.flow.collectLatest

class BacktestGraphFragment : Fragment(R.layout.fragment_backtest_graph) {

    private val vm: BacktestViewModel by activityViewModels()
    private lateinit var web: EquityWebView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        web = view.findViewById(R.id.equityWebView)
        web.initEquity()

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.state.collectLatest { st ->
                val title = "Equity • ${st.instrument} ${st.granularity}"
                web.setTitleText(title)
                web.setEquityJson(st.equityCurveJson)
            }
        }
    }
}
