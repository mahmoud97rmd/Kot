package com.tradingapp.metatrader.app.features.backtest.ui.journal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tradingapp.metatrader.app.databinding.ItemBacktestJournalBinding

class BacktestJournalAdapter : RecyclerView.Adapter<BacktestJournalAdapter.VH>() {

    private val items = ArrayList<JournalEntry>()

    fun submit(list: List<JournalEntry>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemBacktestJournalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size

    class VH(private val b: ItemBacktestJournalBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(e: JournalEntry) {
            b.line1.text = "${e.timeSec} - ${e.title}"
            b.line2.text = e.details
        }
    }
}
