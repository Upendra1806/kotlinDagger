package com.juliusbaer.premarket.core.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.telephony.TelephonyManager

/**
 * Check connection type
 *
 * @author Q-DTY
 */
object ConnectionTypeHelper {

    private const val OFFLINE = "offline"
    private const val WIFI = "wifi"

    private val CONNECTION_TYPES_MAPPING = mutableMapOf<Int,String>()
    init {
        with(CONNECTION_TYPES_MAPPING){
            put(TelephonyManager.NETWORK_TYPE_GPRS, "GPRS")
            put(TelephonyManager.NETWORK_TYPE_EDGE, "EDGE")
            put(TelephonyManager.NETWORK_TYPE_UMTS, "WCDMA")
            put(TelephonyManager.NETWORK_TYPE_HSDPA, "HSDPA")
            put(TelephonyManager.NETWORK_TYPE_HSUPA, "HSUPA")
            put(TelephonyManager.NETWORK_TYPE_HSPA, "HRPD")
            put(TelephonyManager.NETWORK_TYPE_CDMA, "CDMA1x")
            put(TelephonyManager.NETWORK_TYPE_EVDO_0, "CDMAEVDORev0")
            put(TelephonyManager.NETWORK_TYPE_EVDO_A, "CDMAEVDORevA")
            put(TelephonyManager.NETWORK_TYPE_EVDO_B, "CDMAEVDORevB")
            put(TelephonyManager.NETWORK_TYPE_1xRTT, "CDMA1x")
            put(TelephonyManager.NETWORK_TYPE_LTE, "LTE")
            put(TelephonyManager.NETWORK_TYPE_EHRPD, "HRPD")
            put(TelephonyManager.NETWORK_TYPE_IDEN, "HRPD")
            put(TelephonyManager.NETWORK_TYPE_HSPAP, "HRPD")
        }

    }

    /**
     * Return current human-readable connection type
     *
     * @param context app context
     * @return current human-readable connection type
     */
    fun getConnectionTypeHumanReadable(context: Context): String {

        if (!isConnected(context)) {
            return OFFLINE
        }

        if (isConnectedWiFi(context)) {
            return WIFI
        }

        return if (isConnectedMobile(context)) {
            convertMobileConnectionType(getNetworkInfo(context)!!.subtype)
        } else {
            OFFLINE
        }
    }

    /**
     * Get the network info
     *
     * @param context app context
     * @return [NetworkInfo] instance
     */
    private fun getNetworkInfo(context: Context): NetworkInfo? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo
    }

    /**
     * Check if there is any connectivity
     *
     * @param context app context
     * @return true if connected to any network, false otherwise
     */
    fun isConnected(context: Context): Boolean {
        val info = getNetworkInfo(context)
        return info != null && info.isConnected
    }

    /**
     * Check if there is any connectivity to a Wifi network
     *
     * @param context app context
     * @return true if connected to Wi-Fi, false otherwise
     */
    private fun isConnectedWiFi(context: Context): Boolean {
        val cm = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
        return cm.allNetworks.any {
            cm.getNetworkInfo(it).isConnected && cm.getNetworkCapabilities(it).hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        }
    }

    /**
     * Check if there is any connectivity to a mobile network
     *
     * @param context app context
     * @return true if connected to mobile network, false otherwise
     */
    private fun isConnectedMobile(context: Context): Boolean {
        val cm = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
        return cm.allNetworks.any {
            cm.getNetworkInfo(it).isConnected && cm.getNetworkCapabilities(it).hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        }
    }

    private fun convertMobileConnectionType(connectionType: Int): String {
        return if (CONNECTION_TYPES_MAPPING.containsKey(connectionType)) CONNECTION_TYPES_MAPPING[connectionType]!! else OFFLINE
    }
}