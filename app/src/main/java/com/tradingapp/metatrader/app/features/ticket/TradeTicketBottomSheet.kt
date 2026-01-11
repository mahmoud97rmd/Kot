package com.tradingapp.metatrader.app.features.ticket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tradingapp.metatrader.app.databinding.BottomsheetTradeTicketBinding
import com.tradingapp.metatrader.app.state.AppStateViewModel
import com.tradingapp.metatrader.domain.models.trading.PendingOrder
import com.tradingapp.metatrader.domain.models.trading.Position
import com.tradingapp.metatrader.domain.utils.risk.LotCalculator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

@AndroidEntryPoint
class TradeTicketBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomsheetTradeTicketBinding? = null
    private val binding get() = _binding!!

    private val vm: TradeTicketViewModel by viewModels()
    private val appState: AppStateViewModel by activityViewModels()

    private val types = listOf("MARKET", "BUY_LIMIT", "SELL_LIMIT", "BUY_STOP", "SELL_STOP")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomsheetTradeTicketBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.orderTypeSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, types)
        binding.orderTypeSpinner.setSelection(0)

        val mode = arguments?.getString(ARG_MODE) ?: MODE_NEW
        val instrumentArg = arguments?.getString(ARG_INSTRUMENT)?.trim().orEmpty()
        val positionId = arguments?.getString(ARG_POSITION_ID)
        val pendingId = arguments?.getString(ARG_PENDING_ID)

        val startInstrument = if (instrumentArg.isNotEmpty()) instrumentArg else appState.selectedInstrument.value
        binding.instrumentInput.setText(startInstrument)

        val prefTarget = arguments?.getString("pref_target")?.toDoubleOrNull()
        val prefSl = arguments?.getString("pref_sl")?.toDoubleOrNull()
        val prefTp = arguments?.getString("pref_tp")?.toDoubleOrNull()
        if (prefTarget != null) binding.targetPriceInput.setText(prefTarget.toString())
        if (prefSl != null) binding.slInput.setText(prefSl.toString()) else binding.slInput.setText("")
        if (prefTp != null) binding.tpInput.setText(prefTp.toString()) else binding.tpInput.setText("")

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            appState.selectedTick.collectLatest { t ->
                binding.liveLine.text =
                    if (t == null) "Live: --"
                    else String.format(Locale.US, "Live: %s  Bid %.5f  Ask %.5f", t.instrument, t.bid, t.ask)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.status.collectLatest { binding.statusText.text = it }
        }

        applyModeUi(mode)

        binding.calcLotsBtn.setOnClickListener { calcLots(mode) }

        when (mode) {
            MODE_NEW -> {
                binding.buyBtn.setOnClickListener { submitNew(Position.Side.BUY) }
                binding.sellBtn.setOnClickListener { submitNew(Position.Side.SELL) }
            }
            MODE_MODIFY_POSITION -> {
                binding.buyBtn.setOnClickListener {
                    val sl = binding.slInput.text?.toString()?.trim()?.toDoubleOrNull()
                    val tp = binding.tpInput.text?.toString()?.trim()?.toDoubleOrNull()
                    if (positionId.isNullOrBlank()) {
                        binding.statusText.text = "Status: missing positionId"
                        return@setOnClickListener
                    }
                    vm.modifyPositionRisk(positionId, sl, tp)
                    dismiss()
                }
            }
            MODE_MODIFY_PENDING -> {
                binding.buyBtn.setOnClickListener {
                    val target = binding.targetPriceInput.text?.toString()?.trim()?.toDoubleOrNull()
                    if (target == null) {
                        binding.statusText.text = "Status: target required"
                        return@setOnClickListener
                    }
                    val sl = binding.slInput.text?.toString()?.trim()?.toDoubleOrNull()
                    val tp = binding.tpInput.text?.toString()?.trim()?.toDoubleOrNull()
                    if (pendingId.isNullOrBlank()) {
                        binding.statusText.text = "Status: missing orderId"
                        return@setOnClickListener
                    }
                    vm.modifyPendingOrder(pendingId, target, sl, tp)
                    dismiss()
                }
            }
        }
    }

    private fun calcLots(mode: String) {
        val instrument = binding.instrumentInput.text?.toString()?.trim().orEmpty().ifEmpty { appState.selectedInstrument.value }
        val riskPct = binding.riskPercentInput.text?.toString()?.trim()?.toDoubleOrNull() ?: 1.0
        val balance = binding.balanceInput.text?.toString()?.trim()?.toDoubleOrNull() ?: 10_000.0

        val sl = binding.slInput.text?.toString()?.trim()?.toDoubleOrNull()
        if (sl == null) {
            binding.statusText.text = "Status: enter SL to calculate lots"
            return
        }

        val orderType = if (mode == MODE_NEW) (binding.orderTypeSpinner.selectedItem?.toString() ?: "MARKET") else "MARKET"

        val entry = if (orderType == "MARKET") {
            val tick = appState.prices.value[instrument]
            if (tick == null) {
                binding.statusText.text = "Status: no live price for $instrument"
                return
            }
            // Use mid price approximation for calc
            (tick.bid + tick.ask) / 2.0
        } else {
            val target = binding.targetPriceInput.text?.toString()?.trim()?.toDoubleOrNull()
            if (target == null) {
                binding.statusText.text = "Status: target required for pending calc"
                return
            }
            target
        }

        val lots = LotCalculator.calcLots(
            instrument = instrument,
            balance = balance,
            riskPercent = riskPct,
            entryPrice = entry,
            stopLossPrice = sl
        )

        binding.lotsInput.setText(String.format(Locale.US, "%.2f", lots))
        binding.statusText.text = "Status: lots calculated"
    }

    private fun applyModeUi(mode: String) {
        when (mode) {
            MODE_NEW -> {
                binding.title.text = "Trade Ticket"
                binding.sellBtn.isVisible = true
                binding.buyBtn.text = "BUY"
                binding.sellBtn.text = "SELL"

                binding.orderTypeSpinner.isVisible = true
                binding.targetPriceInput.isVisible = true
                binding.lotsInput.isVisible = true

                binding.instrumentInput.isEnabled = true
                binding.orderTypeSpinner.isEnabled = true
                binding.targetPriceInput.isEnabled = true
                binding.lotsInput.isEnabled = true

                binding.calcLotsBtn.isVisible = true
                binding.riskPercentInput.isVisible = true
                binding.balanceInput.isVisible = true
            }

            MODE_MODIFY_POSITION -> {
                binding.title.text = "Modify Position"
                binding.sellBtn.isVisible = false
                binding.buyBtn.text = "SAVE"

                binding.orderTypeSpinner.isVisible = false
                binding.targetPriceInput.isVisible = false
                binding.lotsInput.isVisible = false

                binding.instrumentInput.isEnabled = false

                // Risk calc hidden in modify (MT5 عادة يعرضه في نافذة أخرى)
                binding.calcLotsBtn.isVisible = false
                binding.riskPercentInput.isVisible = false
                binding.balanceInput.isVisible = false
            }

            MODE_MODIFY_PENDING -> {
                binding.title.text = "Modify Pending Order"
                binding.sellBtn.isVisible = false
                binding.buyBtn.text = "SAVE"

                binding.orderTypeSpinner.isVisible = false
                binding.targetPriceInput.isVisible = true
                binding.lotsInput.isVisible = false

                binding.instrumentInput.isEnabled = false
                binding.targetPriceInput.isEnabled = true

                binding.calcLotsBtn.isVisible = true
                binding.riskPercentInput.isVisible = true
                binding.balanceInput.isVisible = true
            }
        }
    }

    private fun submitNew(side: Position.Side) {
        val instrument = binding.instrumentInput.text?.toString()?.trim().orEmpty().ifEmpty { appState.selectedInstrument.value }
        appState.selectInstrument(instrument)

        val tick = appState.prices.value[instrument]
        if (tick == null) {
            binding.statusText.text = "Status: no live price for $instrument"
            return
        }

        val lots = binding.lotsInput.text?.toString()?.trim()?.toDoubleOrNull() ?: 1.0
        val sl = binding.slInput.text?.toString()?.trim()?.toDoubleOrNull()
        val tp = binding.tpInput.text?.toString()?.trim()?.toDoubleOrNull()
        val mode = binding.orderTypeSpinner.selectedItem?.toString() ?: "MARKET"

        val marketPrice = if (side == Position.Side.BUY) tick.ask else tick.bid
        if (!validateRisk(side, marketPrice, sl, tp)) return

        if (mode == "MARKET") {
            vm.submitMarket(instrument, side, marketPrice, lots, sl, tp)
            dismiss()
            return
        }

        val target = binding.targetPriceInput.text?.toString()?.trim()?.toDoubleOrNull()
        if (target == null) {
            binding.statusText.text = "Status: target price required for pending"
            return
        }

        val type = PendingOrder.Type.valueOf(mode)
        if (!validatePending(type, tick, target)) return

        val impliedSide = when (type) {
            PendingOrder.Type.BUY_LIMIT, PendingOrder.Type.BUY_STOP -> Position.Side.BUY
            PendingOrder.Type.SELL_LIMIT, PendingOrder.Type.SELL_STOP -> Position.Side.SELL
        }
        if (!validateRisk(impliedSide, target, sl, tp)) return

        vm.submitPending(instrument, type, target, lots, sl, tp)
        dismiss()
    }

    private fun validateRisk(side: Position.Side, price: Double, sl: Double?, tp: Double?): Boolean {
        if (sl != null && tp != null && sl == tp) {
            binding.statusText.text = "Status: SL and TP cannot be equal"
            return false
        }
        return if (side == Position.Side.BUY) {
            if (sl != null && sl >= price) {
                binding.statusText.text = "Status: BUY requires SL < price"
                false
            } else if (tp != null && tp <= price) {
                binding.statusText.text = "Status: BUY requires TP > price"
                false
            } else true
        } else {
            if (sl != null && sl <= price) {
                binding.statusText.text = "Status: SELL requires SL > price"
                false
            } else if (tp != null && tp >= price) {
                binding.statusText.text = "Status: SELL requires TP < price"
                false
            } else true
        }
    }

    private fun validatePending(type: PendingOrder.Type, tick: com.tradingapp.metatrader.domain.models.Tick, target: Double): Boolean {
        val bid = tick.bid
        val ask = tick.ask

        return when (type) {
            PendingOrder.Type.BUY_LIMIT -> {
                if (target >= ask) { binding.statusText.text = "Status: BUY LIMIT must be below current ask"; false } else true
            }
            PendingOrder.Type.SELL_LIMIT -> {
                if (target <= bid) { binding.statusText.text = "Status: SELL LIMIT must be above current bid"; false } else true
            }
            PendingOrder.Type.BUY_STOP -> {
                if (target <= ask) { binding.statusText.text = "Status: BUY STOP must be above current ask"; false } else true
            }
            PendingOrder.Type.SELL_STOP -> {
                if (target >= bid) { binding.statusText.text = "Status: SELL STOP must be below current bid"; false } else true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_MODE = "mode"
        private const val ARG_INSTRUMENT = "instrument"
        private const val ARG_POSITION_ID = "positionId"
        private const val ARG_PENDING_ID = "pendingId"

        const val MODE_NEW = "NEW"
        const val MODE_MODIFY_POSITION = "MODIFY_POSITION"
        const val MODE_MODIFY_PENDING = "MODIFY_PENDING"

        fun newTrade(instrument: String?): TradeTicketBottomSheet {
            return TradeTicketBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_MODE, MODE_NEW)
                    putString(ARG_INSTRUMENT, instrument)
                }
            }
        }

        fun modifyPosition(instrument: String, positionId: String, sl: Double?, tp: Double?): TradeTicketBottomSheet {
            return TradeTicketBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_MODE, MODE_MODIFY_POSITION)
                    putString(ARG_INSTRUMENT, instrument)
                    putString(ARG_POSITION_ID, positionId)
                    putString("pref_sl", sl?.toString())
                    putString("pref_tp", tp?.toString())
                }
            }
        }

        fun modifyPending(instrument: String, pendingId: String, target: Double, sl: Double?, tp: Double?): TradeTicketBottomSheet {
            return TradeTicketBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_MODE, MODE_MODIFY_PENDING)
                    putString(ARG_INSTRUMENT, instrument)
                    putString(ARG_PENDING_ID, pendingId)
                    putString("pref_target", target.toString())
                    putString("pref_sl", sl?.toString())
                    putString("pref_tp", tp?.toString())
                }
            }
        }
    }
}
