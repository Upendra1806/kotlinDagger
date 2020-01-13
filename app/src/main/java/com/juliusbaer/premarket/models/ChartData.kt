package com.juliusbaer.premarket.models

import com.google.gson.annotations.SerializedName

data class ChartData(
        @SerializedName("Data") val data: List<Data>?,
        @SerializedName("XAxisInterval") val xAxisInterval: Int?)

data class Data(
        @SerializedName("X") val x: Long,
        @SerializedName("Y") val y: Float)