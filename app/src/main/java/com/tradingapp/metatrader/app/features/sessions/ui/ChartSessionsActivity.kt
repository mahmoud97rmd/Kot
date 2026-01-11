package com.tradingapp.metatrader.app.features.sessions.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tradingapp.metatrader.app.R
import com.tradingapp.metatrader.app.features.chart.ChartActivity
import com.tradingapp.metatrader.app.features.sessions.model.ChartSession
import com.tradingapp.metatrader.app.features.sessions.store.ChartSessionsStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChartSessionsActivity : AppCompatActivity() {

    @Inject lateinit var store: ChartSessionsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart_sessions)

        val addBtn: android.widget.Button = findViewById(R.id.addBtn)
        val list: RecyclerView = findViewById(R.id.list)

        val adapter = ChartSessionsAdapter(
            onOpen = { openSession(it) },
            onRename = { renameSession(it) },
            onDelete = { deleteSession(it) }
        )

        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter

        addBtn.setOnClickListener { createNewDialog() }

        lifecycleScope.launchWhenStarted {
            store.sessionsFlow.collectLatest { sessions ->
                adapter.submit(sessions)
            }
        }
    }

    private fun openSession(s: ChartSession) {
        lifecycleScope.launch {
            store.touch(s.id)
            val itn = Intent(this@ChartSessionsActivity, ChartActivity::class.java)
            itn.putExtra(ChartActivity.EXTRA_SYMBOL, s.symbol)
            itn.putExtra(ChartActivity.EXTRA_TIMEFRAME, s.timeframe)
            startActivity(itn)
        }
    }

    private fun deleteSession(s: ChartSession) {
        AlertDialog.Builder(this)
            .setTitle("Delete chart?")
            .setMessage("Delete '${s.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch { store.deleteSession(s.id) }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun renameSession(s: ChartSession) {
        val v = LayoutInflater.from(this).inflate(R.layout.dialog_rename_session, null, false)
        val edit: EditText = v.findViewById(R.id.titleEdit)
        edit.setText(s.title)

        AlertDialog.Builder(this)
            .setTitle("Rename chart")
            .setView(v)
            .setPositiveButton("Save") { _, _ ->
                val t = edit.text.toString().trim()
                lifecycleScope.launch { store.updateTitle(s.id, t) }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createNewDialog() {
        val v = LayoutInflater.from(this).inflate(R.layout.dialog_create_session, null, false)
        val symbolEdit: EditText = v.findViewById(R.id.symbolEdit)
        val tfEdit: EditText = v.findViewById(R.id.tfEdit)
        val titleEdit: EditText = v.findViewById(R.id.titleEdit)

        symbolEdit.setText("XAU_USD")
        tfEdit.setText("M1")
        titleEdit.setText("")

        AlertDialog.Builder(this)
            .setTitle("New Chart")
            .setView(v)
            .setPositiveButton("Create") { _, _ ->
                val sym = symbolEdit.text.toString().trim()
                val tf = tfEdit.text.toString().trim()
                val title = titleEdit.text.toString().trim()

                if (sym.isBlank() || tf.isBlank()) {
                    AlertDialog.Builder(this)
                        .setMessage("Symbol & Timeframe are required.")
                        .setPositiveButton("OK", null)
                        .show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    val s = store.createSession(sym, tf, title.ifBlank { null })
                    openSession(s)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
