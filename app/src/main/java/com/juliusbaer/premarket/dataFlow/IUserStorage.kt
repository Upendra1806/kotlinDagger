package com.juliusbaer.premarket.dataFlow

import com.juliusbaer.premarket.helpers.chart.ChartInterval

interface IUserStorage {
    fun saveToken(token: String)
    fun getToken(): String
    fun setFirstTimeLoading(firstTime: Boolean)
    fun getFirstTimeLoading(): Boolean
    fun savePhoneNumber(number: String)
    fun getPhoneNumber(): String?
    fun getEmail(): String?
    fun saveEmail(email: String?)
    fun saveTokenRole(role: String)
    fun getTokenRole(): String
    fun setIsConfirmed(confirmed: Boolean)
    fun isConfirmed(): Boolean
    fun clearFilter()
    fun setAlphabetic(toBoolean: Boolean)
    fun setTop(toBoolean: Boolean)
    fun setBoxes(toBoolean: Boolean)
    fun setCandles(toBoolean: Boolean)
    fun getAlphabetic(): Boolean
    fun getTop(): Boolean
    fun getBoxes(): Boolean
    fun setUserId(deviceId: String)
    fun getUserID(): String
    fun savePublicKey(publicKey: String)
    fun getPublicKey(): String
    var noInternetAlertShown: Boolean
    var promotionLastModifiedDate: Long
    var badgeCount: Int
    var interval: ChartInterval
    var fxInterval: ChartInterval
}