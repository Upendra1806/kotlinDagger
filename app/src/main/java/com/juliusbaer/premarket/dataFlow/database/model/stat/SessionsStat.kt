package com.juliusbaer.premarket.dataFlow.database.model.stat

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

open class SessionsStat:RealmObject() {
        @SerializedName("ConnectionType")
        var connectionType: String?="" // string
        @SerializedName("EndTime")
        var endTime: Long?=0 // 0
        @SerializedName("StartTime")
        var startTime: Long?=0 // 0
}