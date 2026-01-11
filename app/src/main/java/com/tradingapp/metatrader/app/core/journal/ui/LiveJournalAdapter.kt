package com.tradingapp.metatrader.app.core.journal.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tradingapp.metatrader.app.R
import com.tradingapp.metatrader.app.core.journal.JournalEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LiveJournalAdapter : RecyclerView.Adapter<LiveJournalAdapter.VH>() {

    private val items = ArrayList<JournalEntry>()
    private val fmt = SimpleDateFormat("HH:mm:ss", Locale.US)

    fun add(entry: JournalEntry) {
        items.add(entry)
        if (items.size > 2000) {
            items.subList(0, 200).clear()
        }
        notifyItemInserted(items.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_journal_entry, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val e = items[position]
        holder.meta.text = "${fmt.format(Date(e.timeMs))} • ${e.source} • ${e.level}"
        holder.msg.text = e.message
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val meta: TextView = v.findViewById(R.id.metaText)
        val msg: TextView = v.findViewById(R.id.msgText)
    }
}
