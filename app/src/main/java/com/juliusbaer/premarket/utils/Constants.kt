package com.juliusbaer.premarket.utils

import org.threeten.bp.ZoneId

object Constants {
    const val CLICK_TIME_INTERVAL = 600L
    const val OFFLINE_FRAGMENT: String = "OFFLINE_FRAGMENT"

    const val VOLUME_CRITERION: String = "Volume"
    const val PRICE_CRITERION: String = "Price"
    const val EXPIRY_CRITERION: String = "Expiry"

    const val PUSH_ID_KEY = "ProductId"
    const val DATE_ONLY_FORMAT = "dd/MM/yyyy"
    const val DATE_FORMAT = "dd/MM/yyyy, HH:mm"

    const val NEWS: String = "News"

    const val WARRANTS_CATEGORY_RANGE = 3
    const val WARRANTS_CATEGORY_KNOCK_OUT = 2
    const val WARRANTS_CATEGORY_WARRANTS = 1

    const val SOCKET_TOPIC_DIGITS = 6
    const val PRECISION = 2

    val swissZoneId: ZoneId = ZoneId.of("Europe/Zurich")

    interface ChartConstant{

        companion object{
            val LINE_CHART:String = "lineChart"
            val CANDLE_CHART :String = "candleChart"
        }
    }

    interface ElementType{

        companion object{
            val UNDERLYING:String = "underlying"
            val INDEX :String = "index"
            val CURRENCY:String = "currency"
            val WARRANTS:String = "warrants"
            val FX:String ="fx"
        }
    }
}