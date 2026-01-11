package com.tradingapp.metatrader.app.features.backtest.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.tradingapp.metatrader.app.R
import com.tradingapp.metatrader.app.features.backtest.BacktestViewModel
import com.tradingapp.metatrader.app.features.chart.webview.ChartWebView
import kotlinx.coroutines.flow.collectLatest

class BacktestChartFragment : Fragment(R.layout.fragment_backtest_chart) {

    private val vm: BacktestViewModel by activityViewModels()
    private lateinit var web: ChartWebView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        web = view.findViewById(R.id.chartWebView)
        web.initChart()

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.state.collectLatest { st ->
                // These will be queued if page not ready yet (ChartWebView handles it)
                web.setHistoryJson(st.backtestCandlesJson)
                web.setMarkersJson(st.backtestMarkersJson)
            }
        }
    }
}
