package com.tradingapp.metatrader.app.features.quotes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tradingapp.metatrader.app.databinding.ItemQuoteBinding
import com.tradingapp.metatrader.domain.models.Tick
import com.tradingapp.metatrader.domain.models.market.WatchlistItem
import java.util.Locale

class QuotesAdapter(
    private val onClick: (WatchlistItem) -> Unit,
    private val onLongClick: (WatchlistItem) -> Unit
) : RecyclerView.Adapter<QuotesAdapter.VH>() {

    private val items = mutableListOf<WatchlistItem>()
    private var prices: Map<String, Tick> = emptyMap()

    fun submit(list: List<WatchlistItem>, pricesMap: Map<String, Tick>) {
        items.clear()
        items.addAll(list)
        prices = pricesMap
        notifyDataSetChanged()
    }

    class VH(val b: ItemQuoteBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemQuoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.b.symbol.text = item.displayName
        holder.b.instrument.text = item.instrument

        val t = prices[item.instrument]
        holder.b.price.text = if (t == null) "--"
        else String.format(Locale.US, "Bid %.5f | Ask %.5f", t.bid, t.ask)

        holder.b.root.setOnClickListener { onClick(item) }
        holder.b.root.setOnLongClickListener {
            onLongClick(item)
            true
        }
    }

    override fun getItemCount(): Int = items.size
}
