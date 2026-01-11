package com.tradingapp.metatrader.app.features.trade

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tradingapp.metatrader.app.databinding.ItemPositionLiveBinding
import com.tradingapp.metatrader.domain.models.Tick
import com.tradingapp.metatrader.domain.models.trading.Position
import java.util.Locale

class PositionsLiveAdapter(
    private val onModify: (Position) -> Unit,
    private val onClose: (Position, Double) -> Unit
) : RecyclerView.Adapter<PositionsLiveAdapter.VH>() {

    private val items = mutableListOf<Position>()
    private var prices: Map<String, Tick> = emptyMap()

    fun submit(list: List<Position>, pricesMap: Map<String, Tick>) {
        items.clear()
        items.addAll(list)
        prices = pricesMap
        notifyDataSetChanged()
    }

    class VH(val b: ItemPositionLiveBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemPositionLiveBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]
        holder.b.title.text = "${p.side.name} ${p.instrument}"
        holder.b.sub.text = String.format(Locale.US, "Entry: %.5f  Lots: %.2f  SL: %s  TP: %s",
            p.entryPrice, p.lots,
            p.stopLoss?.let { String.format(Locale.US, "%.5f", it) } ?: "--",
            p.takeProfit?.let { String.format(Locale.US, "%.5f", it) } ?: "--"
        )

        val tick = prices[p.instrument]
        val mark = if (tick == null) null else {
            if (p.side == Position.Side.BUY) tick.bid else tick.ask
        }

        val pl = if (mark == null) null else {
            val points = if (p.side == Position.Side.BUY) (mark - p.entryPrice) else (p.entryPrice - mark)
            points * p.lots * 100.0
        }

        holder.b.pl.text = if (pl == null) "P/L: --"
        else String.format(Locale.US, "P/L: %.2f", pl)

        holder.b.modifyBtn.setOnClickListener { onModify(p) }

        holder.b.closeBtn.setOnClickListener {
            if (mark == null) return@setOnClickListener
            onClose(p, mark)
        }
    }

    override fun getItemCount(): Int = items.size
}
