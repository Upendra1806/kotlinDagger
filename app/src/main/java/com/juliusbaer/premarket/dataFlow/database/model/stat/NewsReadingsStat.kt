package com.juliusbaer.premarket.dataFlow.database.model.stat

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

open class NewsReadingsStat:RealmObject() {
        @SerializedName("NewsId")
        var newsId: Int?=null // 0
        @SerializedName("ReadingLengthSec")
        var readingLengthSec: Long?=null // 0
        @SerializedName("ReadingTime")
        var readingTime: Long?=null // 0
        @SerializedName("SourceTypeId")
        var sourceTypeId: Int? =null// 0
}