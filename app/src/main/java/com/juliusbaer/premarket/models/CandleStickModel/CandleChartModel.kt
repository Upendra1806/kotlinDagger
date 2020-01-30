package com.juliusbaer.premarket.models.CandleStickModel

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index

open class CandleChartModel : RealmObject(){

    @Index
    var id: Int = 0
    @Index
    var period: String = ""
    var interval: Int? = null

    var coords: RealmList<CandleChartCoordModel>? = null
}