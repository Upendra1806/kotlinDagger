package com.juliusbaer.premarket.ui.watchlist

data class WatchListSearchModel(val id: Int, val title: String,val type :String){
    override fun toString(): String {
        return title
    }
}