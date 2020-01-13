package com.juliusbaer.premarket.models.serverModels

import com.google.gson.annotations.SerializedName

data class ProductUpdateModel(
        @SerializedName("Id") val id: Int, //0
        @SerializedName("Title") val title: String, //CH0019399838
        @SerializedName("PriceAsk") val priceAsk: Double, //0.0
        @SerializedName("PriceBid") val priceBid: Double, //0.0
        @SerializedName("PriceChangePct") val priceChangePct: Double, //0.17
        @SerializedName("Valor") val valor: String, //1939983
        @SerializedName("Ticker") val ticker: String, //CH0019399838
        @SerializedName("Identifier") val identifier: String?, //SMIM
        @SerializedName("Isin") val isin: String?, //CH0019399838
        @SerializedName("ProductType") val productType: String?,
        @SerializedName("PriceAskVolume") val priceAskVolume: Long?, //0.0
        @SerializedName("PriceCurrency") val priceCurrency: String?, //CH
        @SerializedName("Open") val priceOpen: Double?, //9583.14
        @SerializedName("PriceBidAskSpreadPct") val priceBidAskSpreadPct: Double?, //0.0
        @SerializedName("PriceBidVolume") val priceBidVolume: Long?, //0.0
        @SerializedName("PriceSettled") val priceSettled: Double?, //2667,391
        @SerializedName("PriceChangeAbs") val priceChangeAbs: Double?, //4.663
        @SerializedName("ImpliedVolatility") val impliedVolatility: Double?, //0.0
        @SerializedName("PriceDateTime") val priceDateTime: Long?, //2018-01-09T13:46:13+01:00
        @SerializedName("LastTraded") val lastTraded: Double?, //2672.054
        @SerializedName("MinLastTraded") val minLastTraded: Double?, //2672.054
        @SerializedName("MaxLastTraded") val maxLastTraded: Double?, //2672.054
        @SerializedName("DistanceToStrikePct") val distanceToStrikePct: Double?,
        @SerializedName("Gearing") val gearing: Double?,
        @SerializedName("Top") val top: Boolean = false,
        @SerializedName("Leverage") val leverage: Double?,
        @SerializedName("Delta") val delta: Double?,
        @SerializedName("TradedVolume") val tradedVolume: Long?
)