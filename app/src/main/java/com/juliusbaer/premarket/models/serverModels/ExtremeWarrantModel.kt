package com.juliusbaer.premarket.models.serverModels

import com.google.gson.annotations.SerializedName

data class ExtremeWarrantModel(
        @SerializedName("ProductLastTraded") val productLastTraded: Double? = null, // 0
        @SerializedName("ProductTitle") val productTitle: String? = null, // string
        @SerializedName("Categories") val categories: List<Int>,
        @SerializedName("TradedVolumeMax")val tradedVolumeMax: Long?, // 0
        @SerializedName("TradedVolumeMin") val tradedVolumeMin: Long?, // 0
        @SerializedName("ProductId") val productId: Int?,
        @SerializedName("UnderlyingId") val underlyingId: Int?,
        @SerializedName("ContractOption") val contractOption: String?,
        @SerializedName("StrikePriceMin") val strikePriceMin: Double?,
        @SerializedName("StrikePriceMax") val strikePriceMax: Double?,
        @SerializedName("DistanceToStrikeMin") val distanceToStrikeMin: Double? = null,
        @SerializedName("DistanceToStrikeMax") val distanceToStrikeMax: Double? = null,
        @SerializedName("TopOnly") val topOnly: Boolean,
        @SerializedName("MaturityStartDate") val maturityStartDate: Long? = null,
        @SerializedName("MaturityEndDate") val maturityEndDate: Long? = null,
        @SerializedName("PageNumber") val pageNumber: Int,
        @SerializedName("PageSize") val pageSize: Int,
        @SerializedName("SortField") val sortField: String,
        @SerializedName("IsAscending") val isAscending: Boolean,
        @SerializedName("WarrantsExist") val warrantsExist: Boolean? = null
)