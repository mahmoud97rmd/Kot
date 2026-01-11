package com.tradingapp.metatrader.app.features.terminal.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.tradingapp.metatrader.app.R
import com.tradingapp.metatrader.app.features.editor.ui.MetaEditorActivity
import com.tradingapp.metatrader.app.features.journal.ui.LogsActivity
import com.tradingapp.metatrader.app.features.oanda.settings.ui.OandaSettingsActivity
import com.tradingapp.metatrader.app.features.tester.ui.StrategyTesterActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<Button>(R.id.oandaBtn).setOnClickListener {
            startActivity(Intent(requireContext(), OandaSettingsActivity::class.java))
        }
        view.findViewById<Button>(R.id.editorBtn).setOnClickListener {
            startActivity(Intent(requireContext(), MetaEditorActivity::class.java))
        }
        view.findViewById<Button>(R.id.testerBtn).setOnClickListener {
            startActivity(Intent(requireContext(), StrategyTesterActivity::class.java))
        }
        view.findViewById<Button>(R.id.logsBtn).setOnClickListener {
            startActivity(Intent(requireContext(), LogsActivity::class.java))
        }
    }
}
