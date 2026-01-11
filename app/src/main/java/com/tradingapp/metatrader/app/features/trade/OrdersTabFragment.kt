package com.tradingapp.metatrader.app.features.trade

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.lifecycleScope
import com.tradingapp.metatrader.app.databinding.FragmentOrdersTabBinding
import com.tradingapp.metatrader.app.features.ticket.TradeTicketBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class OrdersTabFragment : Fragment() {

    private var _binding: FragmentOrdersTabBinding? = null
    private val binding get() = _binding!!

    private val vm: OrdersTabViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOrdersTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = PendingOrdersLiveAdapter(
            onModify = { o ->
                TradeTicketBottomSheet
                    .modifyPending(o.instrument, o.id, o.targetPrice, o.stopLoss, o.takeProfit)
                    .show(parentFragmentManager, "ticket_modify_pending")
            },
            onCancel = { vm.cancel(it) }
        )
        binding.ordersList.layoutManager = LinearLayoutManager(requireContext())
        binding.ordersList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.orders.collectLatest { adapter.submit(it) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
