package com.tradingapp.metatrader.app.features.journal.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tradingapp.metatrader.app.R
import com.tradingapp.metatrader.app.features.journal.logs.ExpertsBus
import com.tradingapp.metatrader.app.features.journal.logs.JournalBus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class LogsActivity : AppCompatActivity() {

    @Inject lateinit var journal: JournalBus
    @Inject lateinit var experts: ExpertsBus

    private enum class Mode { EXPERTS, JOURNAL }

    private var mode: Mode = Mode.EXPERTS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)

        val expertsBtn: Button = findViewById(R.id.expertsBtn)
        val journalBtn: Button = findViewById(R.id.journalBtn)
        val clearBtn: Button = findViewById(R.id.clearBtn)

        val statusText: TextView = findViewById(R.id.statusText)
        val list: ListView = findViewById(R.id.logsList)

        fun bind(flow: StateFlow<List<com.tradingapp.metatrader.app.features.journal.logs.LogEntry>>) {
            lifecycleScope.launch {
                flow.collectLatest { entries ->
                    val fmt = SimpleDateFormat("HH:mm:ss", Locale.US)
                    val items = entries.takeLast(500).reversed().map { e ->
                        val t = fmt.format(Date(e.timeMs))
                        val scope = buildString {
                            if (e.expertName != null) append("[${e.expertName}] ")
                            if (e.symbol != null && e.timeframe != null) append("${e.symbol} ${e.timeframe} ")
                        }
                        "$t ${e.level} ${scope}${e.message}"
                    }
                    list.adapter = ArrayAdapter(this@LogsActivity, android.R.layout.simple_list_item_1, items)
                    statusText.text = "Status: ${mode.name} | lines=${entries.size}"
                }
            }
        }

        expertsBtn.setOnClickListener {
            mode = Mode.EXPERTS
            statusText.text = "Status: Experts"
            bind(experts.entries)
        }

        journalBtn.setOnClickListener {
            mode = Mode.JOURNAL
            statusText.text = "Status: Journal"
            bind(journal.entries)
        }

        clearBtn.setOnClickListener {
            if (mode == Mode.EXPERTS) experts.clear() else journal.clear()
        }

        // default
        bind(experts.entries)
    }
}
