package com.juliusbaer.premarket.dataFlow.database.model.stat

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey

open class StatsCollection : RealmObject() {
    @SerializedName("AppVersion")
    var appVersion: String? = null // string

    @SerializedName("Device")
    var device: String? = null // string

    @SerializedName("DeviceType")
    var deviceType: String? = null // string

    @PrimaryKey
    @SerializedName("GUID")
    var gUID: String? = null // 00000000-0000-0000-0000-000000000000

    @SerializedName("Language")
    var language: String? = null // string

    @SerializedName("LocalTime")
    var localTime: Long? = null // 0

    @SerializedName("Manufacturer")
    var manufacturer: String? = null // string

    @Ignore
    @SerializedName("NewsReadingsStat")
    var newsReadingsStat: List<NewsReadingsStat?>? = ArrayList()

    @SerializedName("OS")
    var oS: String? = null // string

    @Ignore
    @SerializedName("ProductsStat")
    var productsStat: List<ProductsStat?>? = ArrayList()

    @SerializedName("Region")
    var region: String? = null // string

    @SerializedName("PushNotificationsEnabled")
    var pushNotificationsEnabled: Boolean = false

    @Ignore
    @SerializedName("SessionsStat")
    var sessionsStat: List<SessionsStat?>? = ArrayList()
}