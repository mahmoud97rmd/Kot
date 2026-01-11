package com.tradingapp.metatrader.app.features.oanda.settings.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tradingapp.metatrader.app.R
import com.tradingapp.metatrader.app.features.oanda.net.OandaRestTester
import com.tradingapp.metatrader.app.features.oanda.settings.OandaSettingsStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OandaSettingsActivity : AppCompatActivity() {

    @Inject lateinit var store: OandaSettingsStore
    @Inject lateinit var tester: OandaRestTester

    private val envs = listOf("practice", "live")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oanda_settings)

        val envSpinner: Spinner = findViewById(R.id.envSpinner)
        val tokenEdit: EditText = findViewById(R.id.tokenEdit)
        val accountEdit: EditText = findViewById(R.id.accountEdit)
        val saveBtn: Button = findViewById(R.id.saveBtn)
        val testBtn: Button = findViewById(R.id.testBtn)
        val fetchAccountsBtn: Button = findViewById(R.id.fetchAccountsBtn)
        val statusText: TextView = findViewById(R.id.statusText)
        val rawText: TextView = findViewById(R.id.rawText)

        envSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, envs)

        lifecycleScope.launchWhenStarted {
            store.settingsFlow.collectLatest { s ->
                val idx = envs.indexOf(s.env.lowercase()).let { if (it < 0) 0 else it }
                if (envSpinner.selectedItemPosition != idx) envSpinner.setSelection(idx)
                if (tokenEdit.text.toString() != s.token) tokenEdit.setText(s.token)
                if (accountEdit.text.toString() != s.accountId) accountEdit.setText(s.accountId)
            }
        }

        saveBtn.setOnClickListener {
            val env = envs[envSpinner.selectedItemPosition.coerceIn(0, envs.lastIndex)]
            val token = tokenEdit.text.toString()
            val accountId = accountEdit.text.toString()

            lifecycleScope.launch {
                store.setEnv(env)
                store.setToken(token)
                store.setAccountId(accountId)
                statusText.text = "Status: Saved."
            }
        }

        testBtn.setOnClickListener {
            statusText.text = "Status: Testing..."
            rawText.text = "Raw: --"
            lifecycleScope.launch {
                // Ensure current UI values are stored before testing
                val env = envs[envSpinner.selectedItemPosition.coerceIn(0, envs.lastIndex)]
                store.setEnv(env)
                store.setToken(tokenEdit.text.toString())
                store.setAccountId(accountEdit.text.toString())

                val res = tester.testAccountSummary()
                statusText.text = "Status: ${res.message}"
                rawText.text = "Raw: " + (res.raw ?: "--").take(2500)
            }
        }

        fetchAccountsBtn.setOnClickListener {
            statusText.text = "Status: Fetching accounts..."
            rawText.text = "Raw: --"
            lifecycleScope.launch {
                val env = envs[envSpinner.selectedItemPosition.coerceIn(0, envs.lastIndex)]
                store.setEnv(env)
                store.setToken(tokenEdit.text.toString())
                store.setAccountId(accountEdit.text.toString())

                val res = tester.fetchAccounts()
                statusText.text = "Status: ${res.message}"
                rawText.text = "Raw: " + (res.raw ?: "--").take(2500)
            }
        }
    }
}
