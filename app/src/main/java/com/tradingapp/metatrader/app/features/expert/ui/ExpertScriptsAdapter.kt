package com.tradingapp.metatrader.app.features.expert.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tradingapp.metatrader.app.R
import com.tradingapp.metatrader.domain.models.expert.ExpertScript
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpertScriptsAdapter(
    private val onEdit: (ExpertScript) -> Unit,
    private val onEnable: (ExpertScript) -> Unit,
    private val onAttach: (ExpertScript) -> Unit,
    private val onDelete: (ExpertScript) -> Unit
) : RecyclerView.Adapter<ExpertScriptsAdapter.VH>() {

    private val items = ArrayList<ExpertScript>()
    private val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

    fun submit(list: List<ExpertScript>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_expert_script, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = items[position]
        holder.name.text = s.name
        holder.meta.text = "${s.language.name} • updated ${fmt.format(Date(s.updatedAtMs))}"
        holder.badge.visibility = if (s.isEnabled) View.VISIBLE else View.GONE
        holder.enableBtn.text = if (s.isEnabled) "Enabled" else "Enable"
        holder.enableBtn.isEnabled = !s.isEnabled

        holder.editBtn.setOnClickListener { onEdit(s) }
        holder.enableBtn.setOnClickListener { onEnable(s) }
        holder.attachBtn.setOnClickListener { onAttach(s) }
        holder.deleteBtn.setOnClickListener { onDelete(s) }
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.nameText)
        val meta: TextView = v.findViewById(R.id.metaText)
        val badge: TextView = v.findViewById(R.id.enabledBadge)
        val editBtn: Button = v.findViewById(R.id.editBtn)
        val enableBtn: Button = v.findViewById(R.id.enableBtn)
        val attachBtn: Button = v.findViewById(R.id.attachBtn)
        val deleteBtn: Button = v.findViewById(R.id.deleteBtn)
    }
}
