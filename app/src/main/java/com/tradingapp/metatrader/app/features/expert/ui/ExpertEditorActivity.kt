package com.tradingapp.metatrader.app.features.expert.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tradingapp.metatrader.app.R
import com.tradingapp.metatrader.app.features.expert.templates.DefaultExpertTemplates
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ExpertEditorActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "expert_id"
    }

    private val vm: ExpertEditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expert_editor)

        val id = intent.getStringExtra(EXTRA_ID) ?: run {
            finish()
            return
        }

        val title: TextView = findViewById(R.id.title)
        val subtitle: TextView = findViewById(R.id.subtitle)
        val nameEdit: EditText = findViewById(R.id.nameEdit)
        val codeEdit: EditText = findViewById(R.id.codeEdit)
        val templateBtn: Button = findViewById(R.id.templateBtn)
        val saveBtn: Button = findViewById(R.id.saveBtn)
        val enableBtn: Button = findViewById(R.id.enableBtn)

        vm.load(id)

        templateBtn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Insert Template")
                .setItems(arrayOf("Demo Trade EA")) { _, which ->
                    when (which) {
                        0 -> codeEdit.setText(DefaultExpertTemplates.demoTradeJs)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        saveBtn.setOnClickListener {
            val st = vm.state.value
            val enabled = st.script?.isEnabled ?: false
            vm.save(id, nameEdit.text.toString().trim(), codeEdit.text.toString(), enabled)
            AlertDialog.Builder(this)
                .setMessage("Saved")
                .setPositiveButton("OK", null)
                .show()
        }

        enableBtn.setOnClickListener {
            vm.enableExclusive(id)
            AlertDialog.Builder(this)
                .setMessage("Enabled (exclusive). Now Backtest -> Run EA will use it.")
                .setPositiveButton("OK", null)
                .show()
        }

        lifecycleScope.launchWhenStarted {
            vm.state.collectLatest { st ->
                if (st.error != null) {
                    title.text = "Error"
                    subtitle.text = st.error
                    return@collectLatest
                }

                val s = st.script ?: return@collectLatest
                title.text = "Edit: ${s.name}"
                subtitle.text = "Language: ${s.language.name} • ${if (s.isEnabled) "ENABLED" else "DISABLED"}"

                if (nameEdit.text.isNullOrBlank()) nameEdit.setText(s.name)
                if (codeEdit.text.isNullOrBlank()) codeEdit.setText(s.code)

                enableBtn.text = if (s.isEnabled) "Enabled" else "Enable"
                enableBtn.isEnabled = !s.isEnabled
            }
        }
    }
}
