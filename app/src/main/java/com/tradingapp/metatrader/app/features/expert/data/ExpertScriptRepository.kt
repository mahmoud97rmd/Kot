package com.tradingapp.metatrader.app.features.expert.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class ExpertScriptRepository(private val ctx: Context) {

    private val lock = ReentrantLock()
    private val file: File get() = File(ctx.filesDir, "experts_scripts.json")

    fun getAll(): List<ExpertScript> = lock.withLock {
        readAllLocked()
    }

    fun getById(id: Long): ExpertScript? = lock.withLock {
        readAllLocked().firstOrNull { it.id == id }
    }

    fun create(name: String, content: String): ExpertScript = lock.withLock {
        val list = readAllLocked().toMutableList()
        val nextId = (list.maxOfOrNull { it.id } ?: 0L) + 1L
        val script = ExpertScript(nextId, name.ifBlank { "Expert $nextId" }, content)
        list.add(script)
        writeAllLocked(list)
        script
    }

    fun update(id: Long, name: String, content: String) = lock.withLock {
        val list = readAllLocked().toMutableList()
        val idx = list.indexOfFirst { it.id == id }
        if (idx < 0) throw IllegalArgumentException("Script not found: id=$id")
        list[idx] = list[idx].copy(name = name, content = content)
        writeAllLocked(list)
    }

    fun delete(id: Long) = lock.withLock {
        val list = readAllLocked().filterNot { it.id == id }
        writeAllLocked(list)
    }

    // ----- Internal -----
    private fun readAllLocked(): List<ExpertScript> {
        if (!file.exists()) return emptyList()
        val txt = file.readText(Charsets.UTF_8).trim()
        if (txt.isBlank()) return emptyList()

        val arr = JSONArray(txt)
        val out = ArrayList<ExpertScript>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                ExpertScript(
                    id = o.getLong("id"),
                    name = o.optString("name", "Unnamed"),
                    content = o.optString("content", "")
                )
            )
        }
        return out.sortedBy { it.id }
    }

    private fun writeAllLocked(list: List<ExpertScript>) {
        val arr = JSONArray()
        for (s in list.sortedBy { it.id }) {
            val o = JSONObject()
            o.put("id", s.id)
            o.put("name", s.name)
            o.put("content", s.content)
            arr.put(o)
        }
        file.writeText(arr.toString(), Charsets.UTF_8)
    }
}
