package com.tradingapp.metatrader.app.features.strategysettings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tradingapp.metatrader.app.databinding.BottomsheetStrategySettingsBinding
import com.tradingapp.metatrader.domain.models.strategy.StrategySettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class StrategySettingsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomsheetStrategySettingsBinding? = null
    private val binding get() = _binding!!

    private val vm: StrategySettingsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomsheetStrategySettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.settings.collectLatest { s ->
                binding.riskPercentInput.setText(s.riskPercent.toString())
                binding.atrPeriodInput.setText(s.atrPeriod.toString())
                binding.slAtrMultInput.setText(s.slAtrMult.toString())
                binding.tpAtrMultInput.setText(s.tpAtrMult.toString())
                binding.emaFastInput.setText(s.emaFast.toString())
                binding.emaSlowInput.setText(s.emaSlow.toString())
                binding.stochPeriodInput.setText(s.stochPeriod.toString())
                binding.stochTriggerInput.setText(s.stochTrigger.toString())
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.status.collectLatest { binding.statusText.text = it }
        }

        binding.saveBtn.setOnClickListener {
            val newS = StrategySettings(
                riskPercent = binding.riskPercentInput.text?.toString()?.trim()?.toDoubleOrNull() ?: 1.0,
                atrPeriod = binding.atrPeriodInput.text?.toString()?.trim()?.toIntOrNull() ?: 14,
                slAtrMult = binding.slAtrMultInput.text?.toString()?.trim()?.toDoubleOrNull() ?: 1.5,
                tpAtrMult = binding.tpAtrMultInput.text?.toString()?.trim()?.toDoubleOrNull() ?: 2.0,
                emaFast = binding.emaFastInput.text?.toString()?.trim()?.toIntOrNull() ?: 50,
                emaSlow = binding.emaSlowInput.text?.toString()?.trim()?.toIntOrNull() ?: 150,
                stochPeriod = binding.stochPeriodInput.text?.toString()?.trim()?.toIntOrNull() ?: 14,
                stochTrigger = binding.stochTriggerInput.text?.toString()?.trim()?.toDoubleOrNull() ?: 20.0
            )
            vm.save(newS)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): StrategySettingsBottomSheet = StrategySettingsBottomSheet()
    }
}
