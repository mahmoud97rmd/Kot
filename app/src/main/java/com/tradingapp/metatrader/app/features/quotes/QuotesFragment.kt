package com.tradingapp.metatrader.app.features.quotes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tradingapp.metatrader.app.databinding.FragmentQuotesBinding
import com.tradingapp.metatrader.app.features.ticket.TradeTicketBottomSheet
import com.tradingapp.metatrader.app.state.AppStateViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class QuotesFragment : Fragment() {

    private var _binding: FragmentQuotesBinding? = null
    private val binding get() = _binding!!

    private val vm: QuotesViewModel by viewModels()
    private val appState: AppStateViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentQuotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = QuotesAdapter(
            onClick = { item -> appState.selectInstrument(item.instrument) },
            onLongClick = { item ->
                appState.selectInstrument(item.instrument)
                TradeTicketBottomSheet.newTrade(item.instrument).show(parentFragmentManager, "ticket_new")
            }
        )

        binding.quotesList.layoutManager = LinearLayoutManager(requireContext())
        binding.quotesList.adapter = adapter

        vm.seedIfEmpty()

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            appState.watchlist.collectLatest { wl ->
                adapter.submit(wl, appState.prices.value)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            appState.prices.collectLatest { prices ->
                adapter.submit(appState.watchlist.value, prices)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
