package com.juliusbaer.premarket.models.serverModels

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
open class IndexModel: RealmObject(), Parcelable {
    @PrimaryKey
    @Expose @SerializedName("Id") var id: Int = 0 //53
    @Expose @SerializedName("Valor") var valor: String = "" //998089
    @Expose @SerializedName("Title") var title: String = ""//JB SMI
    @Expose @SerializedName("Name") var name: String? = null //Swiss Market Index (SMI®)
    @Expose @SerializedName("PriceChangePct") var priceChangePct: Double = 0.0 //0.43
    @Expose @SerializedName("LastTraded") var lastTraded: Double? = null //9583.14
    @Expose @SerializedName("PriceSettled") var priceSettled: Double? = null //9583.14
    @Expose @SerializedName("Open") var priceOpen: Double? = null //9583.14
    @Expose @SerializedName("MinLastTraded") var minLastTraded: Double? = null //0
    @Expose @SerializedName("MaxLastTraded") var maxLastTraded: Double? = null //0
    @Expose @SerializedName("PriceCurrency") var priceCurrency: String? = null //CHF
    @Expose @SerializedName("Ticker") var ticker: String? = null //SMI
    @Expose @SerializedName("MarketsTitle") var marketsTitle: String? = null//JB SMI
    @Expose @SerializedName("PriceDateTime") var date: Long? = null //ABB
    @Expose @SerializedName("PriceBidVolume") var priceBidVolume: Long? = null //0
    @Expose @SerializedName("PriceAskVolume") var priceAskVolume: Long? = null //0
    @Expose @SerializedName("InitialReferencePrice") var initialReferencePrice: Double? = null
    @Expose @SerializedName("ImpliedVolatility") var impliedVolatility: Double? = null //0
    @Expose @SerializedName("TradedVolume") var tradedVolume: Long? = null//0
    @Expose @SerializedName("HasDetails") var hasDetails: Boolean? = null //0
    @Expose @SerializedName("NotificationReceived") var notificationReceived: Boolean? = null //0
    @Expose @SerializedName("IsInWatchList") var isInWatchList: Boolean? = null //0
    @Expose @SerializedName("ISIN") var isin: String? = null

    @Index
    var isSmi: Boolean = false

    @Index
    var isMidCap: Boolean = false

    @Index
    var isMarkets: Boolean = false

    fun updateFromSocketModel(socketModel: ProductUpdateModel) {
        title = socketModel.title
        priceChangePct = socketModel.priceChangePct
        lastTraded = socketModel.lastTraded
        priceSettled = socketModel.priceSettled
        priceOpen = socketModel.priceOpen
        minLastTraded = socketModel.minLastTraded
        maxLastTraded = socketModel.maxLastTraded
        date = socketModel.priceDateTime
        priceBidVolume = socketModel.priceBidVolume
        priceAskVolume = socketModel.priceAskVolume
        impliedVolatility = socketModel.impliedVolatility
        tradedVolume = socketModel.tradedVolume
        isin = socketModel.isin
    }

    override fun hashCode(): Int {
        return id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IndexModel

        if (id != other.id) return false
        if (valor != other.valor) return false
        if (title != other.title) return false
        if (name != other.name) return false
        if (priceChangePct != other.priceChangePct) return false
        if (lastTraded != other.lastTraded) return false
        if (priceSettled != other.priceSettled) return false
        if (priceOpen != other.priceOpen) return false
        if (minLastTraded != other.minLastTraded) return false
        if (maxLastTraded != other.maxLastTraded) return false
        if (priceCurrency != other.priceCurrency) return false
        if (ticker != other.ticker) return false
        if (marketsTitle != other.marketsTitle) return false
        if (date != other.date) return false
        if (priceBidVolume != other.priceBidVolume) return false
        if (priceAskVolume != other.priceAskVolume) return false
        if (initialReferencePrice != other.initialReferencePrice) return false
        if (impliedVolatility != other.impliedVolatility) return false
        if (tradedVolume != other.tradedVolume) return false
        if (hasDetails != other.hasDetails) return false
        if (isInWatchList != other.isInWatchList) return false
        if (isin != other.isin) return false

        return true
    }

    override fun toString(): String {
        return "IndexModel(id=$id, valor='$valor', title='$title', name=$name, priceChangePct=$priceChangePct, lastTraded=$lastTraded, priceSettled=$priceSettled, priceOpen=$priceOpen, minLastTraded=$minLastTraded, maxLastTraded=$maxLastTraded, priceCurrency=$priceCurrency, ticker=$ticker, marketsTitle=$marketsTitle, date=$date, priceBidVolume=$priceBidVolume, priceAskVolume=$priceAskVolume, initialReferencePrice=$initialReferencePrice, impliedVolatility=$impliedVolatility, tradedVolume=$tradedVolume, hasDetails=$hasDetails, isInWatchList=$isInWatchList, isin=$isin, isSmi=$isSmi, isMidCap=$isMidCap, isMarkets=$isMarkets)"
    }


}


