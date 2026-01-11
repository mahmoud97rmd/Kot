package com.tradingapp.metatrader.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tradingapp.metatrader.app.databinding.ActivityMainBinding
import com.tradingapp.metatrader.app.features.chart.ChartFragment
import com.tradingapp.metatrader.app.features.history.HistoryFragment
import com.tradingapp.metatrader.app.features.quotes.QuotesFragment
import com.tradingapp.metatrader.app.features.trade.TradeFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.mainContainer.id, ChartFragment())
                .commit()
            binding.bottomNav.selectedItemId = R.id.nav_chart
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_quotes -> QuotesFragment()
                R.id.nav_chart -> ChartFragment()
                R.id.nav_trade -> TradeFragment()
                R.id.nav_history -> HistoryFragment()
                else -> ChartFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(binding.mainContainer.id, fragment)
                .commit()
            true
        }
    }
}
