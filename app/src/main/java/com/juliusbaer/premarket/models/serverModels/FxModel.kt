package com.juliusbaer.premarket.models.serverModels

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey

open class FxModel() : RealmObject(), Parcelable {
    @PrimaryKey
    @Expose
    @SerializedName("Id")
    var id: Int = 0

    @Expose
    @SerializedName("Title")
    var title: String = ""

    @Expose
    @SerializedName("Name")
    var name: String? = null

    @Expose
    @SerializedName("PriceDateTime")
    var priceDateTime: Long? = null

    @Expose
    @SerializedName("PriceChangePct")
    var priceChangePct: Double = 0.0

    @Expose
    @SerializedName("Valor")
    var valor: String? = null

    @Expose
    @SerializedName("Ticker")
    var ticker: String? = null

    @Expose
    @SerializedName("LastTraded")
    var lastTraded: Double? = null

    @Expose
    @SerializedName("Open")
    var priceOpen: Double? = null

    @Expose
    @SerializedName("MaxLastTraded")
    var maxLastTraded: Double? = null

    @Expose
    @SerializedName("MinLastTraded")
    var minLastTraded: Double? = null

    @Expose
    @SerializedName("PriceSettled")
    var priceSettled: Double? = null

    @Expose
    @SerializedName("IsInWatchList")
    var isInWatchList: Boolean? = null //false

    @Expose
    @SerializedName("NotificationReceived")
    var notificationReceived: Boolean? = null //false

    @Expose
    @SerializedName("Precision")
    var precision: Int = 0 //false

    @Expose
    @SerializedName("AveragePriceFor50days")
    var averagePriceFor50days: Double? = null

    @Expose
    @SerializedName("AveragePriceFor100days")
    var averagePriceFor100days: Double? = null

    @Expose
    @SerializedName("AveragePriceFor200days")
    var averagePriceFor200days: Double? = null

    @Index
    var chf: Boolean = false

    @Index
    var usd: Boolean = false

    @Index
    var eur: Boolean = false

    @Index
    var type: Int? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readInt()
        title = parcel.readString()!!
        name = parcel.readString()
        priceDateTime = parcel.readValue(Long::class.java.classLoader) as? Long
        priceChangePct = parcel.readDouble()
        valor = parcel.readString()
        ticker = parcel.readString()
        lastTraded = parcel.readValue(Double::class.java.classLoader) as? Double
        priceOpen = parcel.readValue(Double::class.java.classLoader) as? Double
        maxLastTraded = parcel.readValue(Double::class.java.classLoader) as? Double
        minLastTraded = parcel.readValue(Double::class.java.classLoader) as? Double
        priceSettled = parcel.readValue(Double::class.java.classLoader) as? Double
        isInWatchList = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        notificationReceived = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        precision = parcel.readInt()
        chf = parcel.readByte() != 0.toByte()
        usd = parcel.readByte() != 0.toByte()
        eur = parcel.readByte() != 0.toByte()
        type = parcel.readValue(Int::class.java.classLoader) as? Int
    }

    fun updateFromSocketModel(socketModel: ProductUpdateModel) {
        priceOpen = socketModel.priceOpen
        priceSettled = socketModel.priceSettled
        minLastTraded = socketModel.minLastTraded
        maxLastTraded = socketModel.maxLastTraded
        priceChangePct = socketModel.priceChangePct
        lastTraded = socketModel.lastTraded
        priceDateTime = socketModel.priceDateTime
        title = socketModel.title
    }

    override fun hashCode(): Int {
        return id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FxModel

        if (id != other.id) return false
        if (title != other.title) return false
        if (name != other.name) return false
        if (priceDateTime != other.priceDateTime) return false
        if (priceChangePct != other.priceChangePct) return false
        if (valor != other.valor) return false
        if (ticker != other.ticker) return false
        if (lastTraded != other.lastTraded) return false
        if (priceOpen != other.priceOpen) return false
        if (maxLastTraded != other.maxLastTraded) return false
        if (minLastTraded != other.minLastTraded) return false
        if (priceSettled != other.priceSettled) return false
        if (notificationReceived != other.notificationReceived) return false
        if (isInWatchList != other.isInWatchList) return false
        if (precision != other.precision) return false

        return true
    }

    override fun toString(): String {
        return "FxModel(id=$id, title='$title', name='$name', priceDateTime=$priceDateTime, priceChangePct=$priceChangePct, valor='$valor', ticker='$ticker', lastTraded=$lastTraded, priceOpen=$priceOpen, maxLastTraded=$maxLastTraded, minLastTraded=$minLastTraded, priceSettled=$priceSettled, isInWatchList=$isInWatchList, notificationReceived=$notificationReceived, precision=$precision, chf=$chf, usd=$usd, eur=$eur, type=$type)"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(name)
        parcel.writeValue(priceDateTime)
        parcel.writeDouble(priceChangePct)
        parcel.writeString(valor)
        parcel.writeString(ticker)
        parcel.writeValue(lastTraded)
        parcel.writeValue(priceOpen)
        parcel.writeValue(maxLastTraded)
        parcel.writeValue(minLastTraded)
        parcel.writeValue(priceSettled)
        parcel.writeValue(isInWatchList)
        parcel.writeValue(notificationReceived)
        parcel.writeInt(precision)
        parcel.writeByte(if (chf) 1 else 0)
        parcel.writeByte(if (usd) 1 else 0)
        parcel.writeByte(if (eur) 1 else 0)
        parcel.writeValue(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FxModel> {
        override fun createFromParcel(parcel: Parcel): FxModel {
            return FxModel(parcel)
        }

        override fun newArray(size: Int): Array<FxModel?> {
            return arrayOfNulls(size)
        }
    }
}