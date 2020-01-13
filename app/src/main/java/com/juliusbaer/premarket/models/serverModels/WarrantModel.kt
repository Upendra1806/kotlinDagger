package com.juliusbaer.premarket.models.serverModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.Index
import java.io.Serializable

open class WarrantModel : RealmObject(), Serializable {
    @Expose @Index @SerializedName("Id") var id: Int = 0 //247
    @Expose @SerializedName("Title") var title: String = "" //Call Warrants on ABB
    @Expose @SerializedName("PriceBid") var priceBid: Double = 0.0 //0.32
    @Expose @SerializedName("PriceAsk") var priceAsk: Double = 0.0 //0.33
    @Expose @SerializedName("PriceChangePct") var priceChangePct: Double = 0.0 //-0.08702115

    @Expose @SerializedName("StrikeType") var strikeType: String? = null //Call

    @Expose @SerializedName("ISIN") var isin: String? = null //26.87
    @Expose @SerializedName("Ticker") var ticker: String? = null //ABBAJB
    @Expose @SerializedName("Valor") var valor: String? = null //34855089

    @Expose @SerializedName("IsTop") var isTop: Boolean? = null //247
    @Expose @SerializedName("StrikeLevel") var strikeLevel: Double? = null //160
    @Expose @SerializedName("ExerciseDate") var exerciseDate: Long? = null //1521158400
    @Expose @SerializedName("NotificationReceived") var notificationReceived: Boolean? = null
    @Expose @SerializedName("LastTraded") var lastTraded: Double? = null // 0
    @Expose @SerializedName("TradedVolume") var tradedVolume: Long? = null // 0

    @Expose @SerializedName("Category") var category: Int? = null

    @Index var collectionId: Int? = null
    @Index var isMostActive: Boolean = false

    fun updateFromSocketModel(socketModel: ProductUpdateModel) {
        priceBid = socketModel.priceBid
        priceAsk = socketModel.priceAsk
        priceChangePct = socketModel.priceChangePct
        lastTraded = socketModel.lastTraded
        tradedVolume = socketModel.tradedVolume
    }


    override fun toString(): String {
        return "WarrantModel(id=$id, title='$title', priceBid=$priceBid, priceAsk=$priceAsk, priceChangePct=$priceChangePct, strikeType=$strikeType, isin=$isin, ticker=$ticker, valor=$valor, isTop=$isTop, strikeLevel=$strikeLevel, exerciseDate=$exerciseDate, notificationReceived=$notificationReceived, lastTraded=$lastTraded, tradedVolume=$tradedVolume, category=$category, collectionId=$collectionId, isMostActive=$isMostActive)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WarrantModel

        if (id != other.id) return false
        if (title != other.title) return false
        if (priceBid != other.priceBid) return false
        if (priceAsk != other.priceAsk) return false
        if (priceChangePct != other.priceChangePct) return false
        if (strikeType != other.strikeType) return false
        if (isin != other.isin) return false
        if (ticker != other.ticker) return false
        if (valor != other.valor) return false
        if (isTop != other.isTop) return false
        if (strikeLevel != other.strikeLevel) return false
        if (exerciseDate != other.exerciseDate) return false
        if (notificationReceived != other.notificationReceived) return false
        if (lastTraded != other.lastTraded) return false
        if (tradedVolume != other.tradedVolume) return false
        if (category != other.category) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }
}

