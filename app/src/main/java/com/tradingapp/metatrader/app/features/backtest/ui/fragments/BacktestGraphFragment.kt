package com.tradingapp.metatrader.app.features.backtest.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.tradingapp.metatrader.app.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BacktestGraphFragment : Fragment(android.R.layout.simple_list_item_1) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tv: TextView = view.findViewById(android.R.id.text1)
        tv.setTextColor(0xFFD1D4DC.toInt())
        tv.setBackgroundColor(0xFF0B1220.toInt())
        tv.text = "Graph placeholder.\n(We will render equity curve properly in the next tasks.)"
    }
}
