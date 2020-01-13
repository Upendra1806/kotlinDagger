package com.juliusbaer.premarket.helpers.chart

import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat

class YAxisValueFormatter(var precision: Int) : ValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        val df = DecimalFormat("0." + "0".repeat(precision))
        return df.format(value)
    }
}