package com.juliusbaer.premarket.models.serverModels

import com.google.gson.annotations.SerializedName

data class UserInfoModel(
        @SerializedName("IsNotificationForAlertsEnable") var isNotificationForAlertsEnable: Boolean?, //true
        @SerializedName("IsNotificationForNewsEnable") var isNotificationForNewsEnable: Boolean?, //true
        @SerializedName("UnderlyingDisplayingType") var underlyingDisplayingType: Int?, //true
        @SerializedName("UnderlyingSortingOrder") var underlyingSortingOrder: Int?, //true
        @SerializedName("TraderPhoneNumber") var traderPhoneNumber: String? //true
)