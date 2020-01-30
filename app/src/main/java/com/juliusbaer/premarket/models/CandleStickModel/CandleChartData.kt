package com.juliusbaer.premarket.models.CandleStickModel

import com.google.gson.annotations.SerializedName

data class CandleChartData(
@SerializedName("Data") val data: List<CandleData>?,
@SerializedName("XAxisInterval") val xAxisInterval: Int?)

data class CandleData(
        @SerializedName("ShadowHigh") var shadowHigh:Float,
        @SerializedName("ShadowLow") var shadowLow:Float,
        @SerializedName("Close") var close:Float,
        @SerializedName("Open") var open:Float,
        @SerializedName("X") val x: Long,
        @SerializedName("Y") val y: Float)