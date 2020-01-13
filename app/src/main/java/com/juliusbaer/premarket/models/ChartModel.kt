package com.juliusbaer.premarket.models

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index

open class ChartModel: RealmObject() {
    @Index var id: Int = 0
    @Index var period: String = ""
    var interval: Int? = null

    var coords: RealmList<ChartCoordModel>? = null
}