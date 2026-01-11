package com.tradingapp.metatrader.app.features.backtest.equity

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

class EquityWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : WebView(context, attrs) {

    private var isReady = false
    private var pendingEquityJson: String? = null
    private var pendingTitle: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    fun initEquity() {
        isReady = false
        pendingEquityJson = null
        pendingTitle = null

        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true

        webChromeClient = WebChromeClient()
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                isReady = true
                flushPending()
            }
        }

        loadUrl("file:///android_asset/equity/index.html")
    }

    private fun flushPending() {
        if (!isReady) return
        pendingTitle?.let { evaluateJavascript("setTitle(${jsonString(it)});", null) }
        pendingEquityJson?.let { evaluateJavascript("setEquity($it);", null) }
    }

    fun setTitleText(title: String) {
        pendingTitle = title
        if (isReady) evaluateJavascript("setTitle(${jsonString(title)});", null)
    }

    fun setEquityJson(jsonArray: String) {
        pendingEquityJson = jsonArray
        if (isReady) evaluateJavascript("setEquity($jsonArray);", null)
    }

    private fun jsonString(s: String): String {
        // Safe JS string
        val escaped = s
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
        return "\"$escaped\""
    }
}
