package com.juliusbaer.premarket.utils

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.juliusbaer.premarket.R

enum class FxMetals(val prefix: String,
                    @DrawableRes val iconResId: Int,
                    @StringRes val humanTitleResId: Int) {
    XAU("XAU", R.drawable.ic_metal_xau, R.string.gold),
    XAG("XAG", R.drawable.ic_metal_xag, R.string.silver),
    XPT("XPT", R.drawable.ic_metal_xpt, R.string.platinum),
    XPD("XPD", R.drawable.ic_metal_xpd, R.string.palladium),
}