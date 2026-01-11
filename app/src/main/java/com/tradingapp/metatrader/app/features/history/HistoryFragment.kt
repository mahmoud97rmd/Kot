package com.tradingapp.metatrader.app.features.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tradingapp.metatrader.app.databinding.FragmentHistoryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val vm: HistoryViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val posAdapter = PositionsAdapter()
        val histAdapter = ClosedTradesAdapter()

        binding.positionsList.layoutManager = LinearLayoutManager(requireContext())
        binding.positionsList.adapter = posAdapter

        binding.historyList.layoutManager = LinearLayoutManager(requireContext())
        binding.historyList.adapter = histAdapter

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.account.collectLatest { acc ->
                if (acc == null) binding.accountText.text = "Balance: -- | Equity: --"
                else binding.accountText.text = String.format(Locale.US, "Balance: %.2f | Equity: %.2f", acc.balance, acc.equity)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.positions.collectLatest { posAdapter.submit(it) }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.history.collectLatest { histAdapter.submit(it) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
