package com.juliusbaer.premarket.ui.fragments.extentions

import android.content.res.Resources
import android.widget.TextView
import com.juliusbaer.premarket.R
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

fun TextView.setNumber(value: Long?) {
    val symbols = DecimalFormatSymbols()
    symbols.groupingSeparator = '\''
    val dfDecimal = DecimalFormat("###########0")
    dfDecimal.decimalFormatSymbols = symbols
    dfDecimal.groupingSize = 3
    dfDecimal.isGroupingUsed = true
    this.text = dfDecimal.format(value)+""
}

fun Long.formatDate(format: String): String {
    val df = SimpleDateFormat(format, Locale.ENGLISH)
    return df.format(this.times(1000))
}

fun Long.formatDate(format: String, label: String): String {
    val df = SimpleDateFormat(format, Locale.ENGLISH)
    return "$label ${df.format(this.times(1000))}"
}

fun Double.formatPercent(res: Resources, addPlus: Boolean = true): String {
    val resId = when {
        addPlus && this > 0 -> R.string.fmt_percent_2_plus
        else -> R.string.fmt_percent_2
    }
    return res.getString(resId, this.times(100))
}

fun Double.format(digits: Int?, addPlus: Boolean = false) = java.lang.String.format(Locale.US, (if (this > 0 && addPlus) "+" else "") + "%.${digits}f", this)

fun Double.format(digits: Int?, currency: String?, addPlus: Boolean = false) = java.lang.String.format(Locale.US, (if (this > 0 && addPlus) "+" else "") + "%.${digits}f $currency", this)

fun Double.round(places: Int): Double {
    var bd = BigDecimal(this)
    bd = bd.setScale(places, RoundingMode.HALF_UP)
    return bd.toDouble()
}