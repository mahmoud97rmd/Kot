package com.tradingapp.metatrader.app.features.sessions.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tradingapp.metatrader.app.R
import com.tradingapp.metatrader.app.features.sessions.model.ChartSession

class ChartSessionsAdapter(
    private val onOpen: (ChartSession) -> Unit,
    private val onRename: (ChartSession) -> Unit,
    private val onDelete: (ChartSession) -> Unit
) : RecyclerView.Adapter<ChartSessionsAdapter.VH>() {

    private val items = ArrayList<ChartSession>()

    fun submit(list: List<ChartSession>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_chart_session, parent, false)
        return VH(v, onOpen, onRename, onDelete)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    class VH(
        itemView: View,
        private val onOpen: (ChartSession) -> Unit,
        private val onRename: (ChartSession) -> Unit,
        private val onDelete: (ChartSession) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val title: TextView = itemView.findViewById(R.id.title)
        private val sub: TextView = itemView.findViewById(R.id.sub)
        private val openBtn: Button = itemView.findViewById(R.id.openBtn)
        private val renameBtn: Button = itemView.findViewById(R.id.renameBtn)
        private val deleteBtn: Button = itemView.findViewById(R.id.deleteBtn)

        fun bind(s: ChartSession) {
            title.text = s.title
            sub.text = "${s.symbol} • ${s.timeframe}"

            openBtn.setOnClickListener { onOpen(s) }
            renameBtn.setOnClickListener { onRename(s) }
            deleteBtn.setOnClickListener { onDelete(s) }
        }
    }
}
