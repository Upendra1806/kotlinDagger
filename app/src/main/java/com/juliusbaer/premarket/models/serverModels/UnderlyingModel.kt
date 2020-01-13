package com.juliusbaer.premarket.models.serverModels

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey

open class UnderlyingModel: RealmObject() {
    @SerializedName("Id") @PrimaryKey var id: Int = 0 //120
    @SerializedName("Title") var title: String = "" //Sika
    @SerializedName("Name") var name: String? = null //Sika
    @SerializedName("PriceAsk") var priceAsk: Double = 0.0 //8300
    @SerializedName("PriceBid") var priceBid: Double = 0.0 //8295
    @SerializedName("PriceChangePct") var priceChangePct: Double = 0.0 //-0.45
    @SerializedName("LastTraded") var lastTraded: Double? = null //-37.5
    @SerializedName("Valor") var valor: String = "" //58797
    @SerializedName("Ticker") var ticker: String = "" //SIK SW
    @SerializedName("ISIN") var isin: String? = null
    //list only fields
    @SerializedName("NotificationReceived") var notificationReceived: Boolean? = null //0
    //detail only fields
    @SerializedName("MinLastTraded") var minLastTraded: Double? = null //8295
    @SerializedName("MaxLastTraded") var maxLastTraded: Double? = null //8295
    @SerializedName("PriceAskVolume") var priceAskVolume: Long? = null //3
    @SerializedName("PriceBidVolume") var priceBidVolume: Long? = null //4
    @SerializedName("PriceChangeAbs") var priceChangeAbs: Double? = null //-37.5
    @SerializedName("PriceDateTime") var priceDateTime: Long? = null //-37.5
    @SerializedName("PriceSettled") var priceSettled: Double? = null //-37.5
    @SerializedName("Open") var priceOpen: Double? = null //9583.14
    @SerializedName("InitialReferencePrice") var initialReferencePrice: Double? = null //6705
    @SerializedName("ImpliedVolatility") var impliedVolatility: Double? = null //0
    @SerializedName("PriceCurrency") var priceCurrency: String? = null //CHF
    @SerializedName("TopWarrantsCount") var topWarrantsCount: Int? = null //0
    @SerializedName("IsInWatchList") var isInWatchList: Boolean? = null //false


    @Index
    var isSmi: Boolean = false

    @Index
    var isMidCap: Boolean = false

    fun updateFromSocketModel(socketModel: ProductUpdateModel) {
        priceChangePct = socketModel.priceChangePct
        lastTraded = socketModel.lastTraded
        priceSettled = socketModel.priceSettled
        priceOpen = socketModel.priceOpen
        minLastTraded = socketModel.minLastTraded
        maxLastTraded = socketModel.maxLastTraded
        priceDateTime = socketModel.priceDateTime
        priceBidVolume = socketModel.priceBidVolume
        priceAskVolume = socketModel.priceAskVolume
        impliedVolatility = socketModel.impliedVolatility
        isin = socketModel.isin
    }

    fun isEqual(socketModel: ProductUpdateModel): Boolean {
        if (priceChangePct != socketModel.priceChangePct) return false
        if (lastTraded != socketModel.lastTraded) return false
        if (priceSettled != socketModel.priceSettled) return false
        if (priceOpen != socketModel.priceOpen) return false
        if (minLastTraded != socketModel.minLastTraded) return false
        if (maxLastTraded != socketModel.maxLastTraded) return false
        if (priceDateTime != socketModel.priceDateTime) return false
        if (priceBidVolume != socketModel.priceBidVolume) return false
        if (priceAskVolume != socketModel.priceAskVolume) return false
        if (impliedVolatility != socketModel.impliedVolatility) return false
        if (isin != socketModel.isin) return false

        return true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UnderlyingModel

        if (id != other.id) return false
        if (title != other.title) return false
        if (name != other.name) return false
        if (priceAsk != other.priceAsk) return false
        if (priceBid != other.priceBid) return false
        if (priceChangePct != other.priceChangePct) return false
        if (valor != other.valor) return false
        if (ticker != other.ticker) return false
        if (minLastTraded != other.minLastTraded) return false
        if (maxLastTraded != other.maxLastTraded) return false
        if (priceAskVolume != other.priceAskVolume) return false
        if (priceBidVolume != other.priceBidVolume) return false
        if (priceChangeAbs != other.priceChangeAbs) return false
        if (priceDateTime != other.priceDateTime) return false
        if (priceSettled != other.priceSettled) return false
        if (priceOpen != other.priceOpen) return false
        if (lastTraded != other.lastTraded) return false
        if (initialReferencePrice != other.initialReferencePrice) return false
        if (impliedVolatility != other.impliedVolatility) return false
        if (priceCurrency != other.priceCurrency) return false
        if (topWarrantsCount != other.topWarrantsCount) return false
        if (isin != other.isin) return false
        if (isInWatchList != other.isInWatchList) return false
        if (notificationReceived != other.notificationReceived) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }

    override fun toString(): String {
        return "UnderlyingById(id=$id, title='$title', name=$name, priceAsk=$priceAsk, priceBid=$priceBid, priceChangePct=$priceChangePct, valor='$valor', ticker='$ticker', minLastTraded=$minLastTraded, maxLastTraded=$maxLastTraded, priceAskVolume=$priceAskVolume, priceBidVolume=$priceBidVolume, priceChangeAbs=$priceChangeAbs, priceDateTime=$priceDateTime, priceSettled=$priceSettled, priceOpen=$priceOpen, lastTraded=$lastTraded, initialReferencePrice=$initialReferencePrice, impliedVolatility=$impliedVolatility, priceCurrency=$priceCurrency, topWarrantsCount=$topWarrantsCount, isin=$isin, isInWatchList=$isInWatchList, notificationReceived=$notificationReceived, isSmi=$isSmi, isMidCap=$isMidCap)"
    }
}