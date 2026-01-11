package com.tradingapp.metatrader.app.features.trade

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tradingapp.metatrader.app.databinding.ItemPendingOrderBinding
import com.tradingapp.metatrader.domain.models.trading.PendingOrder
import java.util.Locale

class PendingOrdersAdapter(
    private val onCancel: (String) -> Unit
) : RecyclerView.Adapter<PendingOrdersAdapter.VH>() {

    private val items = mutableListOf<PendingOrder>()

    fun submit(list: List<PendingOrder>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    class VH(val b: ItemPendingOrderBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemPendingOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val o = items[position]
        holder.b.title.text = "${o.type.name} ${o.instrument}"
        holder.b.sub.text = String.format(Locale.US, "Target: %.5f  Lots: %.2f", o.targetPrice, o.lots)
        holder.b.cancelBtn.setOnClickListener { onCancel(o.id) }
    }

    override fun getItemCount(): Int = items.size
}
