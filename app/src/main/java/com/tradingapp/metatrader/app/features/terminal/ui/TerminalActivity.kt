package com.tradingapp.metatrader.app.features.terminal.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tradingapp.metatrader.app.R
import com.tradingapp.metatrader.app.features.sessions.ui.ChartSessionsActivity
import com.tradingapp.metatrader.app.features.terminal.ui.fragments.HistoryFragment
import com.tradingapp.metatrader.app.features.terminal.ui.fragments.QuotesFragment
import com.tradingapp.metatrader.app.features.terminal.ui.fragments.SettingsFragment
import com.tradingapp.metatrader.app.features.terminal.ui.fragments.TradeFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TerminalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terminal)

        val nav: BottomNavigationView = findViewById(R.id.bottomNav)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.terminalContainer, QuotesFragment())
            }
        }

        nav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_quotes -> {
                    supportFragmentManager.commit { replace(R.id.terminalContainer, QuotesFragment()) }
                    true
                }
                R.id.nav_charts -> {
                    startActivity(Intent(this, ChartSessionsActivity::class.java))
                    true
                }
                R.id.nav_trade -> {
                    supportFragmentManager.commit { replace(R.id.terminalContainer, TradeFragment()) }
                    true
                }
                R.id.nav_history -> {
                    supportFragmentManager.commit { replace(R.id.terminalContainer, HistoryFragment()) }
                    true
                }
                R.id.nav_settings -> {
                    supportFragmentManager.commit { replace(R.id.terminalContainer, SettingsFragment()) }
                    true
                }
                else -> false
            }
        }
    }
}
