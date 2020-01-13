package com.juliusbaer.premarket.dataFlow.database.model.stat

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

open class ProductsStat:RealmObject() {
        @SerializedName("EventTypeId")
        var eventTypeId: Int?=null // 0
        @SerializedName("ProductId")
        var productId: Int?=null // 0
        @SerializedName("SourceTypeId")
        var sourceTypeId: Int?=null // 0
}