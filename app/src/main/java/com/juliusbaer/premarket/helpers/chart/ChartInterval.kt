package com.juliusbaer.premarket.helpers.chart

import androidx.annotation.StringRes
import com.juliusbaer.premarket.R

enum class ChartInterval(val v: String, val interval: Int, @StringRes val strResId: Int) {
    INTRADAY("intraday", 3600, R.string.intraday),
    ONE_MONTH("1month", 3600 * 24 * 5, R.string.one_month),
    THREE_MONTH("3months", 3600 * 24 * 7 * 2, R.string.three_months),
    SIX_MONTH("6months", 3600 * 24 * 30, R.string.six_months),
    YEAR("1year", 3600 * 24 * 60, R.string.one_year)
}