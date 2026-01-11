package com.tradingapp.metatrader.app.features.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tradingapp.metatrader.app.databinding.ItemClosedTradeBinding
import com.tradingapp.metatrader.domain.models.trading.ClosedTrade
import java.util.Locale

class ClosedTradesAdapter : RecyclerView.Adapter<ClosedTradesAdapter.VH>() {

    private val items = mutableListOf<ClosedTrade>()

    fun submit(list: List<ClosedTrade>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    class VH(val b: ItemClosedTradeBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemClosedTradeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = items[position]
        holder.b.title.text = "${t.side.name} ${t.instrument}"
        holder.b.sub.text = String.format(Locale.US, "P/L: %.2f  Exit: %.3f", t.profit, t.exitPrice)
    }

    override fun getItemCount(): Int = items.size
}
