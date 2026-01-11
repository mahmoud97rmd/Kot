package com.tradingapp.metatrader.app.features.backtest.inputs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.tradingapp.metatrader.app.databinding.DialogBacktestInputsBinding
import com.tradingapp.metatrader.app.features.backtest.strategy.StrategyType
import com.tradingapp.metatrader.domain.models.backtest.ModelingMode
import kotlin.math.max

class BacktestInputsDialogFragment : DialogFragment() {

    interface Listener {
        fun onSaveInputs(inputs: BacktestInputs)
    }

    private var _binding: DialogBacktestInputsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogBacktestInputsBinding.inflate(LayoutInflater.from(requireContext()))

        val cur = readArgsOrDefault()

        setupStrategySpinner(cur.strategyType)
        setupModelingSpinner(cur.modelingMode)

        bindCurrent(cur)
        applyVisibility(cur.strategyType)

        val dlg = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()

        binding.cancelBtn.setOnClickListener { dismissAllowingStateLoss() }

        binding.saveBtn.setOnClickListener {
            val newInputs = readValidated(cur)
            val listener = (activity as? Listener) ?: (parentFragment as? Listener)
            listener?.onSaveInputs(newInputs)
            dismissAllowingStateLoss()
        }

        return dlg
    }

    private fun setupStrategySpinner(selected: StrategyType) {
        val items = listOf("EMA Cross", "RSI Reversal", "Stochastic Cross")
        binding.strategySpinner.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, items)

        val idx = when (selected) {
            StrategyType.EMA_CROSS -> 0
            StrategyType.RSI_REVERSAL -> 1
            StrategyType.STOCH_CROSS -> 2
        }
        binding.strategySpinner.setSelection(idx)

        binding.strategySpinner.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val type = when (position) {
                        1 -> StrategyType.RSI_REVERSAL
                        2 -> StrategyType.STOCH_CROSS
                        else -> StrategyType.EMA_CROSS
                    }
                    applyVisibility(type)
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }
    }

    private fun setupModelingSpinner(selected: ModelingMode) {
        val items = listOf("Open prices only (fast)", "Candle extremes (more realistic)")
        binding.modelingSpinner.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, items)

        val idx = when (selected) {
            ModelingMode.OPEN_PRICES_ONLY -> 0
            ModelingMode.CANDLE_EXTREMES -> 1
        }
        binding.modelingSpinner.setSelection(idx)
    }

    private fun applyVisibility(type: StrategyType) {
        binding.groupEma.visibility = if (type == StrategyType.EMA_CROSS) View.VISIBLE else View.GONE
        binding.groupRsi.visibility = if (type == StrategyType.RSI_REVERSAL) View.VISIBLE else View.GONE
        binding.groupStoch.visibility = if (type == StrategyType.STOCH_CROSS) View.VISIBLE else View.GONE
    }

    private fun bindCurrent(i: BacktestInputs) {
        // EMA
        binding.emaFastInput.setText(i.emaFast.toString())
        binding.emaSlowInput.setText(i.emaSlow.toString())

        // RSI
        binding.rsiPeriodInput.setText(i.rsiPeriod.toString())
        binding.rsiOversoldInput.setText(i.rsiOversold.toString())
        binding.rsiOverboughtInput.setText(i.rsiOverbought.toString())

        // Stoch
        binding.stochKInput.setText(i.stochK.toString())
        binding.stochDInput.setText(i.stochD.toString())
        binding.stochOversoldInput.setText(i.stochOversold.toString())
        binding.stochOverboughtInput.setText(i.stochOverbought.toString())

        // Risk/levels
        binding.stopLossPointsInput.setText(i.stopLossPoints.toString())
        binding.takeProfitPointsInput.setText(i.takeProfitPoints.toString())
        binding.riskPercentInput.setText(i.riskPercent.toString())

        // Common
        binding.lotsInput.setText(i.lots.toString())

        binding.initialBalanceInput.setText(i.initialBalance.toString())
        binding.commissionInput.setText(i.commissionPerLot.toString())
        binding.spreadPointsInput.setText(i.spreadPoints.toString())
        binding.slippagePointsInput.setText(i.slippagePoints.toString())
        binding.pointValueInput.setText(i.pointValue.toString())
    }

    private fun selectedStrategyType(): StrategyType {
        return when (binding.strategySpinner.selectedItemPosition) {
            1 -> StrategyType.RSI_REVERSAL
            2 -> StrategyType.STOCH_CROSS
            else -> StrategyType.EMA_CROSS
        }
    }

    private fun selectedModelingMode(): ModelingMode {
        return when (binding.modelingSpinner.selectedItemPosition) {
            0 -> ModelingMode.OPEN_PRICES_ONLY
            else -> ModelingMode.CANDLE_EXTREMES
        }
    }

    private fun readValidated(fallback: BacktestInputs): BacktestInputs {
        val type = selectedStrategyType()
        val modeling = selectedModelingMode()

        val emaFast = binding.emaFastInput.text?.toString()?.trim()?.toIntOrNull() ?: fallback.emaFast
        val emaSlow = binding.emaSlowInput.text?.toString()?.trim()?.toIntOrNull() ?: fallback.emaSlow

        val rsiP = binding.rsiPeriodInput.text?.toString()?.trim()?.toIntOrNull() ?: fallback.rsiPeriod
        val rsiOS = binding.rsiOversoldInput.text?.toString()?.trim()?.toDoubleOrNull() ?: fallback.rsiOversold
        val rsiOB = binding.rsiOverboughtInput.text?.toString()?.trim()?.toDoubleOrNull() ?: fallback.rsiOverbought

        val stK = binding.stochKInput.text?.toString()?.trim()?.toIntOrNull() ?: fallback.stochK
        val stD = binding.stochDInput.text?.toString()?.trim()?.toIntOrNull() ?: fallback.stochD
        val stOS = binding.stochOversoldInput.text?.toString()?.trim()?.toDoubleOrNull() ?: fallback.stochOversold
        val stOB = binding.stochOverboughtInput.text?.toString()?.trim()?.toDoubleOrNull() ?: fallback.stochOverbought

        val slPts = binding.stopLossPointsInput.text?.toString()?.trim()?.toDoubleOrNull() ?: fallback.stopLossPoints
        val tpPts = binding.takeProfitPointsInput.text?.toString()?.trim()?.toDoubleOrNull() ?: fallback.takeProfitPoints
        val riskPct = binding.riskPercentInput.text?.toString()?.trim()?.toDoubleOrNull() ?: fallback.riskPercent

        val lots = binding.lotsInput.text?.toString()?.trim()?.toDoubleOrNull() ?: fallback.lots

        val bal = binding.initialBalanceInput.text?.toString()?.trim()?.toDoubleOrNull() ?: fallback.initialBalance
        val comm = binding.commissionInput.text?.toString()?.trim()?.toDoubleOrNull() ?: fallback.commissionPerLot
        val spr = binding.spreadPointsInput.text?.toString()?.trim()?.toDoubleOrNull() ?: fallback.spreadPoints
        val slp = binding.slippagePointsInput.text?.toString()?.trim()?.toDoubleOrNull() ?: fallback.slippagePoints
        val pv = binding.pointValueInput.text?.toString()?.trim()?.toDoubleOrNull() ?: fallback.pointValue

        val safeLots = lots.coerceIn(0.01, 100.0)

        val safeBal = bal.coerceAtLeast(0.0)
        val safeComm = comm.coerceAtLeast(0.0)
        val safeSpr = spr.coerceAtLeast(0.0)
        val safeSlp = slp.coerceAtLeast(0.0)
        val safePv = pv.coerceAtLeast(0.0000001)

        val safeEmaFast = max(1, emaFast)
        val safeEmaSlow = max(safeEmaFast + 1, emaSlow)

        val safeRsiP = max(2, rsiP)
        val safeRsiOS = rsiOS.coerceIn(0.0, 50.0)
        val safeRsiOB = rsiOB.coerceIn(50.0, 100.0)

        val safeStK = max(2, stK)
        val safeStD = max(1, stD)
        val safeStOS = stOS.coerceIn(0.0, 50.0)
        val safeStOB = stOB.coerceIn(50.0, 100.0)

        val safeSlPts = slPts.coerceAtLeast(0.0)
        val safeTpPts = tpPts.coerceAtLeast(0.0)
        val safeRisk = riskPct.coerceIn(0.0, 100.0)

        return BacktestInputs(
            strategyType = type,
            modelingMode = modeling,

            emaFast = safeEmaFast,
            emaSlow = safeEmaSlow,

            rsiPeriod = safeRsiP,
            rsiOversold = safeRsiOS,
            rsiOverbought = safeRsiOB,

            stochK = safeStK,
            stochD = safeStD,
            stochOversold = safeStOS,
            stochOverbought = safeStOB,

            stopLossPoints = safeSlPts,
            takeProfitPoints = safeTpPts,
            riskPercent = safeRisk,

            lots = safeLots,

            initialBalance = safeBal,
            commissionPerLot = safeComm,
            spreadPoints = safeSpr,
            slippagePoints = safeSlp,
            pointValue = safePv
        )
    }

    private fun readArgsOrDefault(): BacktestInputs {
        val a = arguments ?: return BacktestInputs()

        val typeName = a.getString(ARG_STRATEGY, StrategyType.EMA_CROSS.name)
        val type = runCatching { StrategyType.valueOf(typeName) }.getOrElse { StrategyType.EMA_CROSS }

        val modelingName = a.getString(ARG_MODELING, ModelingMode.CANDLE_EXTREMES.name)
        val modeling = runCatching { ModelingMode.valueOf(modelingName) }.getOrElse { ModelingMode.CANDLE_EXTREMES }

        return BacktestInputs(
            strategyType = type,
            modelingMode = modeling,

            emaFast = a.getInt(ARG_FAST, 10),
            emaSlow = a.getInt(ARG_SLOW, 30),

            rsiPeriod = a.getInt(ARG_RSI_P, 14),
            rsiOversold = a.getDouble(ARG_RSI_OS, 30.0),
            rsiOverbought = a.getDouble(ARG_RSI_OB, 70.0),

            stochK = a.getInt(ARG_STK, 14),
            stochD = a.getInt(ARG_STD, 3),
            stochOversold = a.getDouble(ARG_STOS, 20.0),
            stochOverbought = a.getDouble(ARG_STOB, 80.0),

            stopLossPoints = a.getDouble(ARG_SLP, 0.0),
            takeProfitPoints = a.getDouble(ARG_TPP, 0.0),
            riskPercent = a.getDouble(ARG_RISK, 0.0),

            lots = a.getDouble(ARG_LOTS, 1.0),

            initialBalance = a.getDouble(ARG_BAL, 10_000.0),
            commissionPerLot = a.getDouble(ARG_COMM, 0.0),
            spreadPoints = a.getDouble(ARG_SPR, 2.0),
            slippagePoints = a.getDouble(ARG_SLIP, 0.5),
            pointValue = a.getDouble(ARG_PV, 0.01)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_STRATEGY = "strategy"
        private const val ARG_MODELING = "modeling"

        private const val ARG_FAST = "fast"
        private const val ARG_SLOW = "slow"

        private const val ARG_RSI_P = "rsi_p"
        private const val ARG_RSI_OS = "rsi_os"
        private const val ARG_RSI_OB = "rsi_ob"

        private const val ARG_STK = "st_k"
        private const val ARG_STD = "st_d"
        private const val ARG_STOS = "st_os"
        private const val ARG_STOB = "st_ob"

        private const val ARG_SLP = "sl_points"
        private const val ARG_TPP = "tp_points"
        private const val ARG_RISK = "risk_pct"

        private const val ARG_LOTS = "lots"
        private const val ARG_BAL = "bal"
        private const val ARG_COMM = "comm"
        private const val ARG_SPR = "spr"
        private const val ARG_SLIP = "slip"
        private const val ARG_PV = "pv"

        fun newInstance(current: BacktestInputs): BacktestInputsDialogFragment {
            return BacktestInputsDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_STRATEGY, current.strategyType.name)
                    putString(ARG_MODELING, current.modelingMode.name)

                    putInt(ARG_FAST, current.emaFast)
                    putInt(ARG_SLOW, current.emaSlow)

                    putInt(ARG_RSI_P, current.rsiPeriod)
                    putDouble(ARG_RSI_OS, current.rsiOversold)
                    putDouble(ARG_RSI_OB, current.rsiOverbought)

                    putInt(ARG_STK, current.stochK)
                    putInt(ARG_STD, current.stochD)
                    putDouble(ARG_STOS, current.stochOversold)
                    putDouble(ARG_STOB, current.stochOverbought)

                    putDouble(ARG_SLP, current.stopLossPoints)
                    putDouble(ARG_TPP, current.takeProfitPoints)
                    putDouble(ARG_RISK, current.riskPercent)

                    putDouble(ARG_LOTS, current.lots)

                    putDouble(ARG_BAL, current.initialBalance)
                    putDouble(ARG_COMM, current.commissionPerLot)
                    putDouble(ARG_SPR, current.spreadPoints)
                    putDouble(ARG_SLIP, current.slippagePoints)
                    putDouble(ARG_PV, current.pointValue)
                }
            }
        }
    }
}
