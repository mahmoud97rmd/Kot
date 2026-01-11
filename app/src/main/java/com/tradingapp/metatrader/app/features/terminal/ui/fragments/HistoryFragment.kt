package com.tradingapp.metatrader.app.features.terminal.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.tradingapp.metatrader.app.R
import com.tradingapp.metatrader.app.features.terminal.tradinghub.TradingHub
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.roundToLong
import javax.inject.Inject

@AndroidEntryPoint
class HistoryFragment : Fragment(R.layout.fragment_history) {

    @Inject lateinit var hub: TradingHub

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val summary: TextView = view.findViewById(R.id.summaryText)
        val list: ListView = view.findViewById(R.id.dealsList)

        fun fmt2(v: Double): String = ((v * 100.0).roundToLong() / 100.0).toString()

        lifecycleScope.launch {
            hub.state.collectLatest { st ->
                summary.text = "Deals: ${st.deals.size}"
                val items = st.deals.takeLast(300).reversed().map { d ->
                    "${d.symbol} ${d.side} lots=${d.lots} profit=${fmt2(d.profit)} comm=${fmt2(d.commission)} reason=${d.reason}"
                }
                list.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)
            }
        }
    }
}
