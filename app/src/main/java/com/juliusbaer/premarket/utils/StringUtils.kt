package com.juliusbaer.premarket.utils

import java.text.Normalizer

fun String.getNormalizedString(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFD).replace("[^\\p{ASCII}]".toRegex(), "")
}