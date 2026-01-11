package com.tradingapp.metatrader.app.features.backtest.trades

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tradingapp.metatrader.app.R
import java.util.Locale

class TradesAdapter : RecyclerView.Adapter<TradesAdapter.VH>() {

    private val items = ArrayList<UiTrade>()

    fun submit(list: List<UiTrade>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_trade, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val line1: TextView = itemView.findViewById(R.id.line1)
        private val line2: TextView = itemView.findViewById(R.id.line2)
        private val line3: TextView = itemView.findViewById(R.id.line3)

        fun bind(t: UiTrade) {
            line1.text = "${t.side} • ${t.id}"
            line2.text = "Entry: ${t.entryTimeSec} @ ${fmt(t.entryPrice)} | Exit: ${t.exitTimeSec} @ ${fmt(t.exitPrice)}"
            val pnl = fmt(t.profit)
            line3.text = "PnL: $pnl | Reason: ${t.reason}"
            line3.setTextColor(if (t.profit >= 0) 0xFF4CAF50.toInt() else 0xFFFF5252.toInt())
        }

        private fun fmt(x: Double): String = String.format(Locale.US, "%.2f", x)
    }
}
