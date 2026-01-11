package com.tradingapp.metatrader.app.features.backtest.ui.results

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tradingapp.metatrader.app.databinding.ItemBacktestTradeBinding
import com.tradingapp.metatrader.domain.models.backtest.BacktestTrade
import java.util.Locale

class BacktestTradesAdapter(
    private val onClick: (BacktestTrade) -> Unit
) : RecyclerView.Adapter<BacktestTradesAdapter.VH>() {

    private val items = ArrayList<BacktestTrade>()

    fun submit(list: List<BacktestTrade>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemBacktestTradeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    class VH(
        private val b: ItemBacktestTradeBinding,
        private val onClick: (BacktestTrade) -> Unit
    ) : RecyclerView.ViewHolder(b.root) {

        fun bind(t: BacktestTrade) {
            b.topLine.text = "${t.id} ${t.side.name} " + String.format(Locale.US, "%.2f", t.lots)
            b.midLine.text =
                "Entry: ${t.entryTimeSec} @ " + String.format(Locale.US, "%.5f", t.entryPrice) +
                " | Exit: ${t.exitTimeSec} @ " + String.format(Locale.US, "%.5f", t.exitPrice)

            val green = 0xFF4CAF50.toInt()
            val red = 0xFFEF5350.toInt()

            b.profitLine.setTextColor(if (t.profit >= 0.0) green else red)
            b.profitLine.text = "Profit: " + String.format(Locale.US, "%+.2f", t.profit)

            b.root.setOnClickListener { onClick(t) }
        }
    }
}
