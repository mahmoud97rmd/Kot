package com.tradingapp.metatrader.app.core.journal.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tradingapp.metatrader.app.R
import com.tradingapp.metatrader.app.core.journal.LiveJournalBus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class LiveJournalActivity : AppCompatActivity() {

    @Inject lateinit var journal: LiveJournalBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_journal)

        val list: RecyclerView = findViewById(R.id.list)
        val adapter = LiveJournalAdapter()
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter

        lifecycleScope.launchWhenStarted {
            journal.flow.collectLatest { entry ->
                adapter.add(entry)
                list.scrollToPosition(adapter.itemCount - 1)
            }
        }
    }
}
