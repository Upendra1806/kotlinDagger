package com.juliusbaer.premarket.ui.filter

data class FilterSearchModel(val id: Int, val title: String) {
    override fun toString(): String {
        return title
    }
}