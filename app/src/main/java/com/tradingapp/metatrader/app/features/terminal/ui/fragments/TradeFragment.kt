package com.tradingapp.metatrader.app.features.terminal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.tradingapp.metatrader.app.R
import com.tradingapp.metatrader.app.core.trading.commands.OrderCommand
import com.tradingapp.metatrader.app.core.trading.commands.OrderCommandBus
import com.tradingapp.metatrader.app.features.terminal.tradinghub.TradingHub
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TradeFragment : Fragment(R.layout.fragment_trade) {

    @Inject lateinit var hub: TradingHub
    @Inject lateinit var bus: OrderCommandBus

    private enum class Mode { POSITIONS, ORDERS }
    private var mode: Mode = Mode.POSITIONS

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val summary: TextView = view.findViewById(R.id.summaryText)
        val list: ListView = view.findViewById(R.id.mainList)

        val positionsBtn: Button = view.findViewById(R.id.positionsBtn)
        val ordersBtn: Button = view.findViewById(R.id.ordersBtn)

        fun render(st: com.tradingapp.metatrader.app.features.terminal.tradinghub.TradingHubState) {
            when (mode) {
                Mode.POSITIONS -> {
                    summary.text = "Positions: ${st.positions.size} (long-press = manage)"
                    val items = st.positions.map { p ->
                        val sl = p.stopLoss?.toString() ?: "-"
                        val tp = p.takeProfit?.toString() ?: "-"
                        "${p.symbol} ${p.side} lots=${p.lots} entry=${p.entryPrice} SL=$sl TP=$tp (id=${p.id.take(6)})"
                    }
                    list.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)
                }
                Mode.ORDERS -> {
                    summary.text = "Orders: ${st.orders.size} (long-press to cancel)"
                    val items = st.orders.map { o ->
                        val sl = o.stopLoss?.toString() ?: "-"
                        val tp = o.takeProfit?.toString() ?: "-"
                        "${o.symbol} ${o.timeframe} ${o.type} lots=${o.lots} entry=${o.entryPrice} SL=$sl TP=$tp (id=${o.id.take(6)})"
                    }
                    list.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)
                }
            }
        }

        positionsBtn.setOnClickListener {
            mode = Mode.POSITIONS
            lifecycleScope.launch { render(hub.state.value) }
        }
        ordersBtn.setOnClickListener {
            mode = Mode.ORDERS
            lifecycleScope.launch { render(hub.state.value) }
        }

        list.setOnItemLongClickListener { _, _, position, _ ->
            val st = hub.state.value
            when (mode) {
                Mode.ORDERS -> {
                    val order = st.orders.getOrNull(position) ?: return@setOnItemLongClickListener true
                    AlertDialog.Builder(requireContext())
                        .setTitle("Cancel Order")
                        .setMessage("Cancel ${order.type} ${order.symbol} ${order.timeframe} @ ${order.entryPrice}?")
                        .setPositiveButton("Cancel Order") { _, _ ->
                            bus.tryEmit(
                                OrderCommand.CancelPending(
                                    symbol = order.symbol,
                                    timeframe = order.timeframe,
                                    orderId = order.id
                                )
                            )
                        }
                        .setNegativeButton("Close", null)
                        .show()
                    true
                }
                Mode.POSITIONS -> {
                    val pos = st.positions.getOrNull(position) ?: return@setOnItemLongClickListener true
                    val v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_manage_position, null, false)
                    val title: TextView = v.findViewById(R.id.posTitle)
                    val slEdit: EditText = v.findViewById(R.id.slEdit)
                    val tpEdit: EditText = v.findViewById(R.id.tpEdit)
                    val closeLotsEdit: EditText = v.findViewById(R.id.closeLotsEdit)

                    title.text = "Manage ${pos.symbol} ${pos.side} (lots=${pos.lots})"

                    AlertDialog.Builder(requireContext())
                        .setTitle("Position")
                        .setView(v)
                        .setPositiveButton("Apply") { _, _ ->
                            val slTxt = slEdit.text.toString().trim()
                            val tpTxt = tpEdit.text.toString().trim()
                            val clTxt = closeLotsEdit.text.toString().trim()

                            val newSl = slTxt.takeIf { it.isNotEmpty() }?.toDoubleOrNull()
                            val newTp = tpTxt.takeIf { it.isNotEmpty() }?.toDoubleOrNull()

                            if (slTxt.isNotEmpty() && newSl == null) return@setPositiveButton
                            if (tpTxt.isNotEmpty() && newTp == null) return@setPositiveButton

                            if (slTxt.isNotEmpty() || tpTxt.isNotEmpty()) {
                                bus.tryEmit(
                                    OrderCommand.ModifyPositionStops(
                                        symbol = pos.symbol,
                                        positionId = pos.id,
                                        newSl = newSl,
                                        newTp = newTp
                                    )
                                )
                            }

                            val closeLots = clTxt.takeIf { it.isNotEmpty() }?.toDoubleOrNull()
                            if (closeLots != null && closeLots > 0.0) {
                                bus.tryEmit(
                                    OrderCommand.ClosePartial(
                                        symbol = pos.symbol,
                                        positionId = pos.id,
                                        closeLots = closeLots
                                    )
                                )
                            }
                        }
                        .setNegativeButton("Close", null)
                        .show()

                    true
                }
            }
        }

        lifecycleScope.launch {
            hub.state.collectLatest { st -> render(st) }
        }
    }
}
