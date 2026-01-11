package com.tradingapp.metatrader.app.features.backtest.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tradingapp.metatrader.app.databinding.ItemBacktestTradeBinding
import com.tradingapp.metatrader.domain.models.backtest.BacktestTrade
import java.util.Locale

class BacktestTradesAdapter : RecyclerView.Adapter<BacktestTradesAdapter.VH>() {

    private val items = ArrayList<BacktestTrade>()

    fun submit(list: List<BacktestTrade>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemBacktestTradeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class VH(private val b: ItemBacktestTradeBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(t: BacktestTrade) {
            val side = t.side.name
            val profit = String.format(Locale.US, "%.2f", t.profit)
            val entry = String.format(Locale.US, "%.3f", t.entryPrice)
            val exit = String.format(Locale.US, "%.3f", t.exitPrice)

            b.topText.text = "$side | lots=${String.format(Locale.US, "%.2f", t.lots)} | P/L=$profit"
            b.bottomText.text = "Entry $entry @ ${t.entryTimeSec}  →  Exit $exit @ ${t.exitTimeSec}"
        }
    }
}
