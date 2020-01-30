package com.juliusbaer.premarket.models.CandleStickModel

import io.realm.RealmObject

open class CandleChartCoordModel : RealmObject(){

    var x: Long = 0
    var y: Float = 0f
    var shadowHigh:Float = 0f
    var shadowLow:Float = 0f
    var open:Float = 0f
    var close:Float = 0f
}