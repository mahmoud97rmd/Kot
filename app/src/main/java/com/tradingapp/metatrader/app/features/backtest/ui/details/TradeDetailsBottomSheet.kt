package com.tradingapp.metatrader.app.features.backtest.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tradingapp.metatrader.app.databinding.BottomsheetTradeDetailsBinding
import com.tradingapp.metatrader.app.features.backtest.BacktestViewModel
import com.tradingapp.metatrader.domain.models.backtest.BacktestTrade
import java.util.Locale
import kotlin.math.abs

class TradeDetailsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomsheetTradeDetailsBinding? = null
    private val binding get() = _binding!!

    private val vm: BacktestViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomsheetTradeDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val args = requireArguments()
        val id = args.getString(ARG_ID, "--")
        val side = args.getString(ARG_SIDE, "--")
        val lots = args.getDouble(ARG_LOTS, 0.0)
        val entryTime = args.getLong(ARG_ENTRY_TIME, 0L)
        val entryPrice = args.getDouble(ARG_ENTRY_PRICE, 0.0)
        val exitTime = args.getLong(ARG_EXIT_TIME, 0L)
        val exitPrice = args.getDouble(ARG_EXIT_PRICE, 0.0)
        val profit = args.getDouble(ARG_PROFIT, 0.0)
        val sl = args.getDouble(ARG_SL, Double.NaN)
        val tp = args.getDouble(ARG_TP, Double.NaN)

        binding.titleText.text = "Trade $id"

        val res = vm.state.value.result
        val cfg = res?.config

        val commissionCost = (cfg?.commissionPerLot ?: 0.0) * lots
        val spreadCost = (cfg?.spreadPoints ?: 0.0) * (cfg?.pointValue ?: 0.0) * lots
        val slippageCost = 2.0 * (cfg?.slippagePoints ?: 0.0) * (cfg?.pointValue ?: 0.0) * lots
        val totalCosts = commissionCost + spreadCost + slippageCost

        val slText = if (sl.isNaN()) "SL: --" else String.format(Locale.US, "SL: %.5f", sl)
        val tpText = if (tp.isNaN()) "TP: --" else String.format(Locale.US, "TP: %.5f", tp)

        val reason = inferCloseReason(exitPrice, if (sl.isNaN()) null else sl, if (tp.isNaN()) null else tp, cfg?.pointValue ?: 0.0)

        binding.bodyText.text =
            "Side: $side\n" +
            String.format(Locale.US, "Lots: %.2f\n", lots) +
            "Entry: time=$entryTime price=" + String.format(Locale.US, "%.5f", entryPrice) + "\n" +
            "Exit:  time=$exitTime price=" + String.format(Locale.US, "%.5f", exitPrice) + "\n" +
            "Close reason: $reason\n" +
            "Profit (net): " + String.format(Locale.US, "%+.2f", profit) + "\n" +
            slText + "\n" +
            tpText + "\n\n" +
            "Costs (est.):\n" +
            "Commission: " + String.format(Locale.US, "%.2f", commissionCost) + "\n" +
            "Spread:     " + String.format(Locale.US, "%.2f", spreadCost) + "\n" +
            "Slippage:   " + String.format(Locale.US, "%.2f", slippageCost) + "\n" +
            "Total:      " + String.format(Locale.US, "%.2f", totalCosts)

        // Jump buttons
        binding.goEntryBtn.setOnClickListener {
            vm.requestJumpToTime(entryTime)
            dismissAllowingStateLoss()
        }
        binding.goExitBtn.setOnClickListener {
            vm.requestJumpToTime(exitTime)
            dismissAllowingStateLoss()
        }

        updateToggleText()

        binding.toggleLevelsBtn.setOnClickListener {
            vm.setShowLevels(!vm.state.value.showLevels)
            updateToggleText()
        }

        binding.closeBtn.setOnClickListener { dismissAllowingStateLoss() }
    }

    private fun inferCloseReason(exitPrice: Double, sl: Double?, tp: Double?, pointValue: Double): String {
        if (pointValue <= 0.0) return "Normal close"
        val eps = pointValue * 0.5
        if (sl != null && abs(exitPrice - sl) <= eps) return "Stop Loss"
        if (tp != null && abs(exitPrice - tp) <= eps) return "Take Profit"
        return "Normal close"
    }

    private fun updateToggleText() {
        binding.toggleLevelsBtn.text = if (vm.state.value.showLevels) "Hide SL/TP" else "Show SL/TP"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ID = "id"
        private const val ARG_SIDE = "side"
        private const val ARG_LOTS = "lots"
        private const val ARG_ENTRY_TIME = "entry_time"
        private const val ARG_ENTRY_PRICE = "entry_price"
        private const val ARG_EXIT_TIME = "exit_time"
        private const val ARG_EXIT_PRICE = "exit_price"
        private const val ARG_PROFIT = "profit"
        private const val ARG_SL = "sl"
        private const val ARG_TP = "tp"

        fun newInstance(trade: BacktestTrade): TradeDetailsBottomSheet {
            return TradeDetailsBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_ID, trade.id)
                    putString(ARG_SIDE, trade.side.name)
                    putDouble(ARG_LOTS, trade.lots)
                    putLong(ARG_ENTRY_TIME, trade.entryTimeSec)
                    putDouble(ARG_ENTRY_PRICE, trade.entryPrice)
                    putLong(ARG_EXIT_TIME, trade.exitTimeSec)
                    putDouble(ARG_EXIT_PRICE, trade.exitPrice)
                    putDouble(ARG_PROFIT, trade.profit)
                    putDouble(ARG_SL, trade.stopLoss ?: Double.NaN)
                    putDouble(ARG_TP, trade.takeProfit ?: Double.NaN)
                }
            }
        }
    }
}
