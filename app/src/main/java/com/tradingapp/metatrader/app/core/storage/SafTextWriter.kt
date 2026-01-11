package com.tradingapp.metatrader.app.core.storage

import android.content.ContentResolver
import android.net.Uri
import java.io.OutputStreamWriter

object SafTextWriter {
    fun writeText(resolver: ContentResolver, uri: Uri, text: String) {
        resolver.openOutputStream(uri, "w")?.use { os ->
            OutputStreamWriter(os, Charsets.UTF_8).use { w ->
                w.write(text)
            }
        } ?: throw IllegalStateException("Cannot open output stream for uri=$uri")
    }
}
