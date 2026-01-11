package com.tradingapp.metatrader.app.features.trade

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.tradingapp.metatrader.app.databinding.FragmentTradeBinding
import com.tradingapp.metatrader.app.features.trade.history.HistoryTabFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TradeFragment : Fragment() {

    private var _binding: FragmentTradeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTradeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.pager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 3
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> PositionsTabFragment()
                    1 -> PendingTabFragment()
                    else -> HistoryTabFragment()
                }
            }
        }

        TabLayoutMediator(binding.tabs, binding.pager) { tab, pos ->
            tab.text = when (pos) {
                0 -> "Positions"
                1 -> "Pending"
                else -> "History"
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
