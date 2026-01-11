package com.tradingapp.metatrader.app.features.backtest.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.tradingapp.metatrader.app.R
import com.tradingapp.metatrader.app.features.backtest.BacktestViewModel
import kotlinx.coroutines.flow.collectLatest

class BacktestJournalFragment : Fragment(R.layout.fragment_backtest_journal) {

    private val vm: BacktestViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val text: TextView = view.findViewById(R.id.logText)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.state.collectLatest { st ->
                val lines = if (st.expertLogs.isEmpty()) listOf("No logs.") else st.expertLogs
                text.text = lines.joinToString(separator = "\n")
            }
        }
    }
}
