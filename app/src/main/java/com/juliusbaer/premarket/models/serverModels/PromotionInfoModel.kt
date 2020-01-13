package com.juliusbaer.premarket.models.serverModels

import com.google.gson.annotations.SerializedName

data class PromotionInfoModel(
        @SerializedName("Title") val title: String?,
        @SerializedName("Description") val description: String?,
        @SerializedName("Link") val link: String?,
        @SerializedName("ImageUrl") val imageUrl: String?,
        @SerializedName("LastModifiedDate") val lastModifiedDate: Long,
        @SerializedName("FileHash") val fileHash: String?,
        @SerializedName("ShowButton") val showButton: Boolean?,
        @SerializedName("ButtonText") val buttonText: String?,
        @SerializedName("FormattedDescription") val formattedDescription: String?
)