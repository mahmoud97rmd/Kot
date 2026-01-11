package com.tradingapp.metatrader.app.features.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tradingapp.metatrader.app.databinding.ItemPositionBinding
import com.tradingapp.metatrader.domain.models.trading.Position
import java.util.Locale

class PositionsAdapter : RecyclerView.Adapter<PositionsAdapter.VH>() {

    private val items = mutableListOf<Position>()

    fun submit(list: List<Position>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    class VH(val b: ItemPositionBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemPositionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]
        holder.b.title.text = "${p.side.name} ${p.instrument}"
        holder.b.sub.text = String.format(Locale.US, "Entry: %.3f  Lots: %.2f", p.entryPrice, p.lots)
    }

    override fun getItemCount(): Int = items.size
}
