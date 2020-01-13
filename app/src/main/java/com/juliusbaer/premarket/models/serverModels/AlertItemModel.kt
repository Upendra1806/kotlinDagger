package com.juliusbaer.premarket.models.serverModels

import com.google.gson.annotations.SerializedName


data class AlertItemModel(
        @SerializedName("Id") var id: Int?, //0
        @SerializedName("ProductId") var productId: Int?, //0
        @SerializedName("Criterion") var criterion: String?, //string
        @SerializedName("MinValue") var minValue: Double?, //0
        @SerializedName("MaxValue") var maxValue: Double?, //0
        @SerializedName("Value") var value: Double? //0
)