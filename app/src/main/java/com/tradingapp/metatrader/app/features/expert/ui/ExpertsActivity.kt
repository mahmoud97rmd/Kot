package com.tradingapp.metatrader.app.features.expert.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tradingapp.metatrader.app.R
import com.tradingapp.metatrader.app.core.journal.ui.LiveJournalActivity
import com.tradingapp.metatrader.app.features.oanda.settings.ui.OandaSettingsActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ExpertsActivity : AppCompatActivity() {

    private val vm: ExpertsViewModel by viewModels()

    private val instruments = listOf("XAU_USD", "EUR_USD", "GBP_USD", "USD_JPY", "BTC_USD")
    private val timeframes = listOf("M1", "M5", "M15", "H1", "D1")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_experts)

        val list: RecyclerView = findViewById(R.id.list)
        val newBtn: Button = findViewById(R.id.newBtn)
        val oandaBtn: Button = findViewById(R.id.oandaBtn)
        val journalBtn: Button = findViewById(R.id.journalBtn)

        val adapter = ExpertScriptsAdapter(
            onEdit = { script ->
                val itn = Intent(this, ExpertEditorActivity::class.java)
                itn.putExtra(ExpertEditorActivity.EXTRA_ID, script.id)
                startActivity(itn)
            },
            onEnable = { script ->
                vm.enableExclusive(script.id)
            },
            onAttach = { script ->
                showAttachDialog(script.id, script.name)
            },
            onDelete = { script ->
                AlertDialog.Builder(this)
                    .setTitle("Delete Expert")
                    .setMessage("Delete '${script.name}' ?")
                    .setPositiveButton("Delete") { _, _ -> vm.delete(script.id) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter

        newBtn.setOnClickListener {
            val defaultName = "EA_${System.currentTimeMillis()}"
            vm.createNew(defaultName)
        }

        oandaBtn.setOnClickListener {
            startActivity(Intent(this, OandaSettingsActivity::class.java))
        }

        journalBtn.setOnClickListener {
            startActivity(Intent(this, LiveJournalActivity::class.java))
        }

        lifecycleScope.launchWhenStarted {
            vm.scripts.collectLatest { scripts ->
                adapter.submit(scripts)
            }
        }
    }

    private fun showAttachDialog(scriptId: String, scriptName: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_attach_expert, null, false)
        val symbolSp: Spinner = view.findViewById(R.id.symbolSpinner)
        val tfSp: Spinner = view.findViewById(R.id.tfSpinner)

        symbolSp.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, instruments)
        tfSp.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, timeframes)

        AlertDialog.Builder(this)
            .setTitle("Attach: $scriptName")
            .setView(view)
            .setPositiveButton("Attach") { _, _ ->
                val symbol = instruments[symbolSp.selectedItemPosition.coerceIn(0, instruments.lastIndex)]
                val tf = timeframes[tfSp.selectedItemPosition.coerceIn(0, timeframes.lastIndex)]
                vm.attach(scriptId = scriptId, symbol = symbol, timeframe = tf)
                AlertDialog.Builder(this)
                    .setMessage("Attached to $symbol $tf")
                    .setPositiveButton("OK", null)
                    .show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
