package com.juliusbaer.premarket.models.serverModels

import androidx.annotation.StringRes
import com.juliusbaer.premarket.R

enum class FxBaseCurrency(@StringRes val titleResId: Int) {
    CHF(R.string.chf),
    USD(R.string.usd_s),
    EUR(R.string.eur_e)
}