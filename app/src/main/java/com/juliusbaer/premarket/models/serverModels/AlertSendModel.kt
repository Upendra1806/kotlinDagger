package com.juliusbaer.premarket.models.serverModels

import com.google.gson.annotations.SerializedName


data class AlertSendModel(
        @SerializedName("Id") val Id: Int?, //0
//        @SerializedName("ProductId") var productId: Int?, //0
        @SerializedName("Criterion") var criterion: String?,
        @SerializedName("Value") var value: String?
)
