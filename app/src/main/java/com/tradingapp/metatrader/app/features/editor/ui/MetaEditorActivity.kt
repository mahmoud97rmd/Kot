package com.tradingapp.metatrader.app.features.editor.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.tradingapp.metatrader.app.R
import com.tradingapp.metatrader.app.core.expert.dsl.ExpertDslParser
import com.tradingapp.metatrader.app.core.expert.dsl.ExpertDslParseException
import com.tradingapp.metatrader.app.features.expert.data.ExpertScript
import com.tradingapp.metatrader.app.features.expert.data.ExpertScriptRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MetaEditorActivity : AppCompatActivity() {

    @Inject lateinit var repo: ExpertScriptRepository

    private lateinit var listView: ListView
    private lateinit var nameEdit: EditText
    private lateinit var codeEdit: EditText
    private lateinit var statusText: TextView

    private var scripts: List<ExpertScript> = emptyList()
    private var selectedId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meta_editor)

        listView = findViewById(R.id.scriptsList)
        nameEdit = findViewById(R.id.nameEdit)
        codeEdit = findViewById(R.id.codeEdit)
        statusText = findViewById(R.id.statusText)

        val newBtn: Button = findViewById(R.id.newBtn)
        val saveBtn: Button = findViewById(R.id.saveBtn)
        val compileBtn: Button = findViewById(R.id.compileBtn)
        val deleteBtn: Button = findViewById(R.id.deleteBtn)

        refreshList()

        listView.setOnItemClickListener { _, _, position, _ ->
            val s = scripts[position]
            selectedId = s.id
            nameEdit.setText(s.name)
            codeEdit.setText(s.content)
            statusText.text = "Status: Loaded '${s.name}' (id=${s.id})"
        }

        newBtn.setOnClickListener {
            selectedId = null
            nameEdit.setText("New Expert")
            codeEdit.setText(defaultTemplate())
            statusText.text = "Status: New script template"
        }

        saveBtn.setOnClickListener {
            val name = nameEdit.text.toString().trim().ifBlank { "Unnamed Expert" }
            val code = codeEdit.text.toString()

            try {
                val id = selectedId
                if (id == null) {
                    val created = repo.create(name, code)
                    selectedId = created.id
                    statusText.text = "Status: Saved new script id=${created.id}"
                } else {
                    repo.update(id, name, code)
                    statusText.text = "Status: Updated script id=$id"
                }
                refreshList(selectId = selectedId)
            } catch (t: Throwable) {
                statusText.text = "Status: Save error"
                showMsg("Save Error", t.message ?: "Unknown error")
            }
        }

        compileBtn.setOnClickListener {
            val code = codeEdit.text.toString()
            try {
                val model = ExpertDslParser().parse(code)
                statusText.text = "Status: Compiled OK (${model.name}), rules=${model.rules.size}"
                showMsg("Compile", "Compiled OK.\nName: ${model.name}\nRules: ${model.rules.size}\nInputs: ${model.inputs.size}")
            } catch (e: ExpertDslParseException) {
                statusText.text = "Status: Compile error (line ${e.lineNumber})"
                showMsg("Compile Error", e.message ?: "Parse error")
            } catch (t: Throwable) {
                statusText.text = "Status: Compile error"
                showMsg("Compile Error", t.message ?: "Parse error")
            }
        }

        deleteBtn.setOnClickListener {
            val id = selectedId
            if (id == null) {
                showMsg("Delete", "No script selected.")
                return@setOnClickListener
            }
            AlertDialog.Builder(this)
                .setTitle("Delete script?")
                .setMessage("Delete script id=$id permanently?")
                .setPositiveButton("Delete") { _, _ ->
                    try {
                        repo.delete(id)
                        selectedId = null
                        nameEdit.setText("")
                        codeEdit.setText("")
                        statusText.text = "Status: Deleted script id=$id"
                        refreshList()
                    } catch (t: Throwable) {
                        statusText.text = "Status: Delete error"
                        showMsg("Delete Error", t.message ?: "Unknown error")
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun refreshList(selectId: Long? = null) {
        scripts = repo.getAll()
        val titles = scripts.map { "(${it.id}) ${it.name}" }
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, titles)

        if (selectId != null) {
            val idx = scripts.indexOfFirst { it.id == selectId }
            if (idx >= 0) {
                listView.setSelection(idx)
            }
        }

        if (scripts.isEmpty()) {
            statusText.text = "Status: No scripts. Press New."
        }
    }

    private fun defaultTemplate(): String {
        return """
            # name: EMA Cross Demo
            input lot=0.10

            rule BUY when ema(20) crosses_above ema(50)
            rule SELL when ema(20) crosses_below ema(50)
        """.trimIndent()
    }

    private fun showMsg(title: String, msg: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton("OK", null)
            .show()
    }
}
