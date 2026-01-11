package com.tradingapp.metatrader.app.features.trade.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tradingapp.metatrader.app.databinding.ItemTradeHistoryBinding
import com.tradingapp.metatrader.domain.models.trading.Trade
import java.util.Locale

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.VH>() {

    private var items: List<Trade> = emptyList()

    fun submit(list: List<Trade>) {
        items = list.sortedByDescending { it.exitTime.epochSecond }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemTradeHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    class VH(private val b: ItemTradeHistoryBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(t: Trade) {
            b.title.text = "${t.instrument} ${t.side.name}"
            b.sub.text = String.format(
                Locale.US,
                "Entry %.3f  Exit %.3f",
                t.entryPrice,
                t.exitPrice
            )
            val pnl = t.profit
            b.pnl.text = String.format(Locale.US, "%+.2f", pnl)
            b.pnl.setTextColor(if (pnl >= 0) 0xFF26A69A.toInt() else 0xFFEF5350.toInt())
        }
    }
}
