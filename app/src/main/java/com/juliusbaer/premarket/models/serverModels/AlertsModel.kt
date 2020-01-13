package com.juliusbaer.premarket.models.serverModels

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

open class AlertsModel: RealmObject() {
    @SerializedName("ProductId") var productId: Int = 0 //5
    @SerializedName("ProductName") var productName: String = "" //Banco Santander SA
    @SerializedName("ProductType") var productType: Int? = null
    @SerializedName("Precision") var precision: Int? = null
}