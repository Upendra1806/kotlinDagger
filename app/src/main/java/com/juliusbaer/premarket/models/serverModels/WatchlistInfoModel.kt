package com.juliusbaer.premarket.models.serverModels

import com.google.gson.annotations.SerializedName

data class WatchlistInfoModel(
        @SerializedName("Warrants") val warrants: List<WarrantModel>?,
        @SerializedName("Underlyings") val underlyings: List<UnderlyingModel>?,
        @SerializedName("Indexes") val index: List<IndexModel>?,
        @SerializedName("CurrencyPairs") val currencyPairs: List<FxModel>?,
        @SerializedName("PreciousMetals") val preciousMetals: List<FxModel>?)
