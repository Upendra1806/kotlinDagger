package com.juliusbaer.premarket.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FilterModel(
        @SerializedName("UnderlyingId") val underlyingId: Int? = null,
        val underlyingTitle: String? = null,
        @SerializedName("ContractOption") val contractOption: String? = null,
        @SerializedName("MaturityStartDate") val maturityStartDate: Long? = null,
        @SerializedName("MaturityEndDate") val maturityEndDate: Long? = null,
        @SerializedName("StrikePriceMin") val strikePriceMin: Double? = null,
        @SerializedName("StrikePriceMax") val strikePriceMax: Double? = null,
        @SerializedName("TopOnly") val topOnly: Boolean? = null,
        @SerializedName("PageNumber") val paginationStart: Int? = null,
        @SerializedName("PageSize") val paginationCount: Int? = null,
        @SerializedName("Categories") val category: List<Int>? = null,
        @SerializedName("TradedVolumeMin") val tradedVolume: Int? = null,
        @SerializedName("TradedVolumeMax") val tradedVolumeMax: Int? = null
): Parcelable