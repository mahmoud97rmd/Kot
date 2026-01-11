package com.tradingapp.metatrader.app.features.backtest

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.tradingapp.metatrader.app.R
import com.tradingapp.metatrader.app.core.autotrading.AutoTradingOrchestrator
import com.tradingapp.metatrader.app.core.autotrading.AutoTradingStore
import com.tradingapp.metatrader.app.core.journal.ui.LiveJournalActivity
import com.tradingapp.metatrader.app.features.backtest.export.BacktestExporter
import com.tradingapp.metatrader.app.features.backtest.inputs.BacktestInputs
import com.tradingapp.metatrader.app.features.backtest.inputs.BacktestInputsDialogFragment
import com.tradingapp.metatrader.app.features.backtest.ui.BacktestPagerAdapter
import com.tradingapp.metatrader.app.features.expert.ui.ExpertsActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BacktestActivity : AppCompatActivity(), BacktestInputsDialogFragment.Listener {

    private val vm: BacktestViewModel by viewModels()

    @Inject lateinit var autoTradingStore: AutoTradingStore
    @Inject lateinit var autoTradingOrchestrator: AutoTradingOrchestrator

    private val instruments = listOf("XAU_USD", "EUR_USD", "GBP_USD", "USD_JPY", "BTC_USD")
    private val timeframes = listOf("M1", "M5", "M15", "H1", "D1")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backtest)

        autoTradingOrchestrator.startWatching()

        val instrumentSpinner: Spinner = findViewById(R.id.instrumentSpinner)
        val timeframeSpinner: Spinner = findViewById(R.id.timeframeSpinner)
        val rangeBtn: Button = findViewById(R.id.rangeBtn)
        val inputsBtn: Button = findViewById(R.id.inputsBtn)
        val runBtn: Button = findViewById(R.id.runBtn)
        val autoTradingBtn: Button = findViewById(R.id.autoTradingBtn)
        val expertsBtn: Button = findViewById(R.id.expertsBtn)
        val runEaBtn: Button = findViewById(R.id.runEaBtn)
        val journalBtn: Button = findViewById(R.id.journalBtn)
        val exportBtn: Button = findViewById(R.id.exportBtn)
        val progressText: TextView = findViewById(R.id.progressText)

        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)

        viewPager.adapter = BacktestPagerAdapter(this)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Results"
                1 -> "Graph"
                2 -> "Chart"
                3 -> "Journal"
                else -> "Journal"
            }
        }.attach()

        instrumentSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, instruments)
        instrumentSpinner.setSelection(instruments.indexOf(vm.state.value.instrument).coerceAtLeast(0))
        instrumentSpinner.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    vm.setInstrument(instruments[position])
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }

        timeframeSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, timeframes)
        timeframeSpinner.setSelection(timeframes.indexOf(vm.state.value.granularity).coerceAtLeast(0))
        timeframeSpinner.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    vm.setGranularity(timeframes[position])
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }

        rangeBtn.setOnClickListener {
            val picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select backtest range")
                .build()

            picker.addOnPositiveButtonClickListener { range ->
                val fromSec = (range.first / 1000L)
                val toSec = (range.second / 1000L)
                vm.setDateRange(fromSec, toSec)
            }
            picker.show(supportFragmentManager, "date_range")
        }

        inputsBtn.setOnClickListener {
            val current = vm.state.value.inputs
            BacktestInputsDialogFragment
                .newInstance(current)
                .show(supportFragmentManager, "inputs_dialog")
        }

        runBtn.setOnClickListener { vm.runBacktestFromRoomThenAssetsThenDemo() }
        runEaBtn.setOnClickListener { vm.runExpertBacktest() }

        expertsBtn.setOnClickListener { startActivity(Intent(this, ExpertsActivity::class.java)) }
        journalBtn.setOnClickListener { startActivity(Intent(this, LiveJournalActivity::class.java)) }

        exportBtn.setOnClickListener {
            val res = vm.state.value.result ?: return@setOnClickListener
            val exporter = BacktestExporter(this)
            val files = exporter.export(res)

            val uris = arrayListOf(files.csvUri, files.jsonUri, files.htmlUri)
            val share = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "application/octet-stream"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_SUBJECT, "Backtest Export")
                putExtra(Intent.EXTRA_TEXT, "Attached: trades.csv + report.json + report.html")
            }
            startActivity(Intent.createChooser(share, "Share backtest files"))
        }

        autoTradingBtn.setOnClickListener {
            lifecycleScope.launch { autoTradingStore.toggle() }
        }

        lifecycleScope.launchWhenStarted {
            autoTradingStore.enabledFlow.collectLatest { enabled ->
                autoTradingBtn.text = if (enabled) "AutoTrading: ON" else "AutoTrading: OFF"
            }
        }

        lifecycleScope.launchWhenStarted {
            vm.state.collectLatest { st ->
                val i: BacktestInputs = st.inputs
                val rangeText = if (st.rangeFromSec != null && st.rangeToSec != null) {
                    "RangeSec=[${st.rangeFromSec}..${st.rangeToSec}]"
                } else "Range=Latest"

                progressText.text =
                    "${st.progress}\n" +
                        "Src: ${st.dataSource}\n" +
                        "Sym=${st.instrument} TF=${st.granularity} | $rangeText\n" +
                        "Strategy=${i.strategyType.name} | Modeling=${i.modelingMode.name}"

                val enabled = !st.running
                runBtn.isEnabled = enabled
                runEaBtn.isEnabled = enabled
                exportBtn.isEnabled = (st.result != null && !st.running)
                inputsBtn.isEnabled = enabled
                rangeBtn.isEnabled = enabled
                expertsBtn.isEnabled = enabled
                journalBtn.isEnabled = true
                instrumentSpinner.isEnabled = enabled
                timeframeSpinner.isEnabled = enabled
            }
        }
    }

    override fun onSaveInputs(inputs: BacktestInputs) {
        vm.updateInputs(inputs)
    }
}
