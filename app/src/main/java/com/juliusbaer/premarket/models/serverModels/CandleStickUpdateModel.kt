package com.juliusbaer.premarket.models.serverModels

import com.google.gson.annotations.SerializedName

data class CandleStickUpdateModel(
    @SerializedName("ProductId") var id: Int,
    @SerializedName("Title") var title: String,
    @SerializedName("LastTraded") var lastTraded: Double,
    @SerializedName("Low") var low: Double,
    @SerializedName("High") var high: Double,
    @SerializedName("Open") var open: Double,
    @SerializedName("Last") var last: Double,
    @SerializedName("Increasing") var increasing: Boolean,
    @SerializedName("PriceChangePct") var priceChangePct: Double)
