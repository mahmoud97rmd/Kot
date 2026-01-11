package com.tradingapp.metatrader.app.features.terminal.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.tradingapp.metatrader.app.R
import com.tradingapp.metatrader.app.data.local.cache.CandleCacheRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.roundToLong

@AndroidEntryPoint
class QuotesFragment : Fragment(R.layout.fragment_quotes) {

    @Inject lateinit var cache: CandleCacheRepository

    private val symbols = listOf("XAU_USD", "EUR_USD", "GBP_USD", "USD_JPY")
    private val timeframe = "M1"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val list: ListView = view.findViewById(R.id.quotesList)
        val refresh: Button = view.findViewById(R.id.refreshBtn)

        fun fmt(v: Double): String = ((v * 100000.0).roundToLong() / 100000.0).toString()

        fun load() {
            lifecycleScope.launch {
                val items = withContext(Dispatchers.IO) {
                    symbols.map { sym ->
                        val last = cache.loadRecentUnified(sym, timeframe, 1).lastOrNull()
                        if (last == null) "$sym  |  (no cache)"
                        else "$sym  |  last=${fmt(last.close)}  t=${last.timeSec}"
                    }
                }
                list.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)
            }
        }

        refresh.setOnClickListener { load() }
        load()
    }
}
