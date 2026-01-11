package com.tradingapp.metatrader.app.features.chart.webview

import android.content.Context
import android.util.AttributeSet
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import com.tradingapp.metatrader.app.features.chart.bridge.ChartBridge

class ChartWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : WebView(context, attrs) {

    val bridge: ChartBridge = ChartBridge()

    fun initChart() {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        webChromeClient = WebChromeClient()

        // expose "Android" to JS
        addJavascriptInterface(bridge, "Android")

        loadUrl("file:///android_asset/chart/index.html")
    }

    fun evalJs(script: String) {
        post { evaluateJavascript(script, null) }
    }

    fun requestCoordToValue(xPx: Float, yPx: Float, onJson: (String) -> Unit) {
        val id = bridge.nextRequestId(onJson)
        val js = "window.requestCoordToValue && window.requestCoordToValue(${id}, ${xPx}, ${yPx});"
        evalJs(js)
    }

    fun addMarkerJson(jsonObj: String) {
        evalJs("window.addMarker && window.addMarker(${jsonObj});")
    }
}
