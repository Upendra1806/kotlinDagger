package com.juliusbaer.premarket.models

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class DetailWarrModel: RealmObject() {
        @PrimaryKey @SerializedName("Id") var id: Int = 0 // 4616
        @SerializedName("Title") var title: String = "" // ABBQJB
        @SerializedName("PriceBid") var priceBid: Double = 0.0 // 0.16000000
        @SerializedName("PriceAsk") var priceAsk: Double = 0.0 // 0.17000000
        @SerializedName("PriceChangePct") var priceChangePct: Double = 0.0 // 0.0

        @SerializedName("Category") var category: Int? = null
        @SerializedName("StrikeType") var strikeType: String? = null // Call Warrants on ABB Ltd

        @SerializedName("ISIN") var isin: String? = null // Call Warrants on ABB Ltd
        @SerializedName("Name") var name: String? = null // Call Warrants on ABB Ltd
        @SerializedName("Ticker") var ticker: String? = null // ABBQJB
        @SerializedName("Valor") var valor: String? = null // 43932526
        @SerializedName("PriceAskVolume") var priceAskVolume: Long? = null // 500000
        @SerializedName("PriceBidVolume") var priceBidVolume: Long? = null // 1000000
        @SerializedName("PriceChangeAbs") var priceChangeAbs: Double? = null // 0.00000000
        @SerializedName("StrikeLevel") var strikeLevel: Double? = null // 20.00000000
        @SerializedName("ExerciseDate") var exerciseDate: Long? = null // 1552608000
        @SerializedName("PriceSettled") var priceSettled: Double? = null // 0.14026500
        @SerializedName("Ratio") var ratio: String? = null // 4:1


        @SerializedName("ImpliedVolatility") var impliedVolatility: Double? = null // 0.27110000
        @SerializedName("Delta") var delta: Double? = null // 0.37800000
        @SerializedName("PriceCurrency") var priceCurrency: String? = null // CHF
        @SerializedName("Leverage") var leverage: Double? = null // 0.00000000
        @SerializedName("Gearing") var gearing: Double? = null // 0.00000000
        @SerializedName("DistanceToStrikePct") var distanceToStrikePct: Double? = null // 0.04820000
        @SerializedName("LastTraded") var lastTraded: Double? = null // 0.16
        @SerializedName("TradedVolume") var tradedVolume: Int? = null // 700000
        @SerializedName("PriceDateTime") var priceDateTime: Long? = null // 1544195489
        @SerializedName("IsInWatchList") var isInWatchList: Boolean? = null // false
        @SerializedName("NotificationReceived") var notificationReceived: Boolean? = null // false

        @SerializedName("ParentName") var parentName: String? = null // Call Warrants on ABB Ltd

        //Range warrants
        //Cap
        @SerializedName("BarrierLevel") var barrierLevel: Double? = null
        //Floor
        @SerializedName("BarrierUpperLevel") var barrierUpperLevel: Double? = null
        //Days within range
        @SerializedName("EventsInRange") var eventsInRange: Int? = null
        //Max payout
        @SerializedName("PaymentCapLevel") var paymentCapLevel: Double? = null
        //Daily payment
        @SerializedName("PaymentAmount") var paymentAmount: Double? = null
        @SerializedName("TradingDaysToMaturity") var tradingDaysToMaturity: Int? = null
}