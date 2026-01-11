package com.tradingapp.metatrader.app.features.chart

import android.webkit.JavascriptInterface

class ChartJsBridge(
    private val onRiskDragged: (kind: String, price: Double) -> Unit,
    private val onDrawingsChanged: (json: String) -> Unit,
    private val onDrawingSelected: (jsonOrEmpty: String) -> Unit
) {
    @JavascriptInterface
    fun onRiskLineDragged(kind: String, price: Double) {
        onRiskDragged(kind, price)
    }

    @JavascriptInterface
    fun onDrawingsChanged(json: String) {
        onDrawingsChanged(json)
    }

    @JavascriptInterface
    fun onDrawingSelected(jsonOrEmpty: String) {
        onDrawingSelected(jsonOrEmpty)
    }
}
