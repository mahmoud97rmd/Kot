package com.tradingapp.metatrader.app.features.trade

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tradingapp.metatrader.app.databinding.FragmentPositionsTabBinding
import com.tradingapp.metatrader.app.features.backtest.BacktestActivity
import com.tradingapp.metatrader.app.features.ticket.TradeTicketBottomSheet
import com.tradingapp.metatrader.app.state.AppStateViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

@AndroidEntryPoint
class PositionsTabFragment : Fragment() {

    private var _binding: FragmentPositionsTabBinding? = null
    private val binding get() = _binding!!

    private val vm: PositionsTabViewModel by viewModels()
    private val appState: AppStateViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPositionsTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = PositionsLiveAdapter(
            onModify = { p ->
                TradeTicketBottomSheet
                    .modifyPosition(p.instrument, p.id, p.stopLoss, p.takeProfit)
                    .show(parentFragmentManager, "ticket_modify_position")
            },
            onClose = { pos, price -> vm.close(pos.id, price) }
        )
        binding.positionsList.layoutManager = LinearLayoutManager(requireContext())
        binding.positionsList.adapter = adapter

        binding.accountLine.setOnLongClickListener {
            startActivity(Intent(requireContext(), BacktestActivity::class.java))
            true
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.account.collectLatest { acc ->
                binding.accountLine.text =
                    if (acc == null) "Balance: -- | Equity: -- (long-press for Backtest)"
                    else String.format(Locale.US, "Balance: %.2f | Equity: %.2f (long-press for Backtest)", acc.balance, acc.equity)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.positions.collectLatest { pos ->
                adapter.submit(pos, appState.prices.value)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            appState.prices.collectLatest { prices ->
                adapter.submit(vm.positions.value, prices)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
