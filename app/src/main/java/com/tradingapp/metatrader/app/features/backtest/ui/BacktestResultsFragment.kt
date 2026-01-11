package com.tradingapp.metatrader.app.features.backtest.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.tradingapp.metatrader.app.R
import com.tradingapp.metatrader.app.features.backtest.BacktestViewModel
import com.tradingapp.metatrader.app.features.backtest.trades.BacktestTradesActivity
import kotlinx.coroutines.flow.collectLatest
import org.json.JSONObject
import java.util.Locale

class BacktestResultsFragment : Fragment(R.layout.fragment_backtest_results) {

    private val vm: BacktestViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val text: TextView = view.findViewById(R.id.resultsText)
        val openTradesBtn: Button = view.findViewById(R.id.openTradesBtn)

        openTradesBtn.setOnClickListener {
            val st = vm.state.value
            val r = st.result
            if (r == null) return@setOnClickListener

            val title = "Backtest_${st.instrument}_${st.granularity}"

            val summary = JSONObject()
            summary.put("totalTrades", r.totalTrades)
            summary.put("winRate", r.winRate)
            summary.put("netProfit", r.netProfit)
            summary.put("maxDrawdown", r.maxDrawdown)

            val cfg = JSONObject()
            cfg.put("initialBalance", r.config.initialBalance)
            cfg.put("spreadPoints", r.config.spreadPoints)
            cfg.put("commissionPerLot", r.config.commissionPerLot)
            cfg.put("pointValue", r.config.pointValue)

            val itn = Intent(requireContext(), BacktestTradesActivity::class.java)
            itn.putExtra(BacktestTradesActivity.EXTRA_TITLE, title)
            itn.putExtra(BacktestTradesActivity.EXTRA_TRADES_JSON, st.tradesJson)
            itn.putExtra(BacktestTradesActivity.EXTRA_SUMMARY_JSON, summary.toString())
            itn.putExtra(BacktestTradesActivity.EXTRA_CONFIG_JSON, cfg.toString())
            startActivity(itn)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.state.collectLatest { st ->
                val r = st.result
                if (r == null) {
                    text.text = "No result yet.\nRun EA Backtest to see results."
                    openTradesBtn.isEnabled = false
                    return@collectLatest
                }
                openTradesBtn.isEnabled = true

                val s = buildString {
                    append("Total Trades: ").append(r.totalTrades).append("\n")
                    append("Win Rate: ").append(String.format(Locale.US, "%.2f", r.winRate * 100)).append("%\n")
                    append("Net Profit: ").append(String.format(Locale.US, "%.2f", r.netProfit)).append("\n")
                    append("Max Drawdown: ").append(String.format(Locale.US, "%.2f", r.maxDrawdown)).append("\n")
                    append("\nConfig:\n")
                    append("Initial Balance: ").append(String.format(Locale.US, "%.2f", r.config.initialBalance)).append("\n")
                    append("Spread Points: ").append(String.format(Locale.US, "%.2f", r.config.spreadPoints)).append("\n")
                    append("Commission/Lot: ").append(String.format(Locale.US, "%.2f", r.config.commissionPerLot)).append("\n")
                    append("Point Value: ").append(String.format(Locale.US, "%.4f", r.config.pointValue)).append("\n")
                }
                text.text = s
            }
        }
    }
}
