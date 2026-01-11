package com.tradingapp.metatrader.app.features.chart.indicators.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tradingapp.metatrader.app.R
import com.tradingapp.metatrader.app.features.chart.indicators.IndicatorConfig

class IndicatorSettingsBottomSheet(
    private val initial: IndicatorConfig,
    private val onApply: (IndicatorConfig) -> Unit
) : BottomSheetDialogFragment(R.layout.bottomsheet_indicators) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val emaEdit: EditText = view.findViewById(R.id.emaEdit)
        val kEdit: EditText = view.findViewById(R.id.stochKEdit)
        val dEdit: EditText = view.findViewById(R.id.stochDEdit)

        emaEdit.setText(initial.emaPeriods.joinToString(","))
        kEdit.setText(initial.stochK.toString())
        dEdit.setText(initial.stochD.toString())

        view.findViewById<Button>(R.id.cancelBtn).setOnClickListener { dismiss() }

        view.findViewById<Button>(R.id.applyBtn).setOnClickListener {
            val ema = parsePeriods(emaEdit.text.toString())
            val k = kEdit.text.toString().trim().toIntOrNull() ?: initial.stochK
            val d = dEdit.text.toString().trim().toIntOrNull() ?: initial.stochD

            val cfg = IndicatorConfig(
                emaPeriods = ema.ifEmpty { initial.emaPeriods },
                stochK = k.coerceAtLeast(1),
                stochD = d.coerceAtLeast(1)
            )
            onApply(cfg)
            dismiss()
        }
    }

    private fun parsePeriods(s: String): List<Int> {
        return s.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { it.toIntOrNull() }
            .filter { it > 0 }
            .distinct()
            .take(6)
    }
}
