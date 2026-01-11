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
class BacktestJournalFragment : Fragment(R.layout.fragment_backtest_journal) {

    private val vm: BacktestViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tv: TextView = view.findViewById(R.id.journalText)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.state.collectLatest { st ->
                val lines = st.expertLogs
                val header = buildString {
                    append("=== JOURNAL ===\n")
                    append("Source: ").append(st.dataSource).append("\n")
                    append("Running: ").append(st.running).append("\n")
                    append("Progress: ").append(st.progress).append("\n")
                    append("Logs: ").append(lines.size).append("\n")
                    append("================\n\n")
                }
                tv.text = header + (if (lines.isEmpty()) "(no expert logs yet)" else lines.joinToString("\n"))
            }
        }
    }
}
