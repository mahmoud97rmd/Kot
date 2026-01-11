package com.tradingapp.metatrader.app.features.trade

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tradingapp.metatrader.app.databinding.FragmentDealsTabBinding
import com.tradingapp.metatrader.app.features.history.ClosedTradesAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class DealsTabFragment : Fragment() {

    private var _binding: FragmentDealsTabBinding? = null
    private val binding get() = _binding!!

    private val vm: DealsTabViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDealsTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = ClosedTradesAdapter()
        binding.dealsList.layoutManager = LinearLayoutManager(requireContext())
        binding.dealsList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.deals.collectLatest { adapter.submit(it) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
