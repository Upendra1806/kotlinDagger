package com.juliusbaer.premarket.models.serverModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.Index

open class CandleStickModel: RealmObject() {
    @Index @Expose @SerializedName("ProductId") var productId: Int = 0
    @Expose @SerializedName("Title") var title: String = ""
    @Expose @SerializedName("PriceChangePct") var priceChangePct: Double = 0.0
    @Expose @SerializedName("High") var high: Double = 0.0
    @Expose @SerializedName("Low") var low: Double = 0.0
    @Expose @SerializedName("Open") var open: Double = 0.0
    @Expose @SerializedName("Last") var last: Double = 0.0
    @Expose @SerializedName("LastTraded") var lastTraded: Double = 0.0
    @Expose @SerializedName("Increasing") var increasing: Boolean = false
    @Index var type: Int = UnderlyingType.SMI.ordinal

    fun updateFromSocketModel(updateModel: CandleStickUpdateModel) {
        title = updateModel.title
        lastTraded = updateModel.lastTraded
        low = updateModel.low
        high = updateModel.high
        open = updateModel.open
        last = updateModel.last
        increasing = updateModel.increasing
        priceChangePct = updateModel.priceChangePct
    }

    fun isEqual(updateModel: CandleStickUpdateModel): Boolean {
        if (title != updateModel.title) return false
        if (lastTraded != updateModel.lastTraded) return false
        if (low != updateModel.low) return false
        if (high != updateModel.high) return false
        if (open != updateModel.open) return false
        if (last != updateModel.last) return false
        if (increasing != updateModel.increasing) return false
        if (priceChangePct != updateModel.priceChangePct) return false

        return true
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CandleStickModel

        if (high != other.high) return false
        if (last != other.last) return false
        if (low != other.low) return false
        if (lastTraded != other.lastTraded) return false
        if (increasing != other.increasing) return false
        if (priceChangePct != other.priceChangePct) return false
        if (title != other.title) return false
        if (productId != other.productId) return false
        if (open != other.open) return false

        return true
    }

    override fun hashCode(): Int {
        return productId
    }

    override fun toString(): String {
        return "CandleStickModel(productId=$productId, title='$title', priceChangePct=$priceChangePct, high=$high, low=$low, open=$open, last=$last, lastTraded=$lastTraded, increasing=$increasing, type=$type)"
    }
}