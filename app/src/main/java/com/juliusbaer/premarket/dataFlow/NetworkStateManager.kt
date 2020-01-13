package com.juliusbaer.premarket.dataFlow

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import com.hadilq.liveevent.LiveEvent
import com.juliusbaer.premarket.utils.UiUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkStateManager @Inject constructor(private val context: Context, private val storage: IUserStorage) {
    private val networkStateLiveDataMut = LiveEvent<Boolean>()
    val networkStateLiveData: LiveData<Boolean>
        get() = networkStateLiveDataMut

    private val networkRequest by lazy {
        NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
    }

    private val receiver: ConnectivityManager.NetworkCallback = object: ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network?) {
            super.onAvailable(network)

            storage.noInternetAlertShown = false

            unregisterReceiver()

            networkStateLiveDataMut.postValue(true)
        }
    }

    init {
        if (!isNetworkAvailable(context)) setOffline()
    }

    private fun unregisterReceiver() {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.unregisterNetworkCallback(receiver)
    }

    fun setOffline() {
        if (networkStateLiveDataMut.value != false) {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (UiUtils.isCompatWithN) {
                cm.registerDefaultNetworkCallback(receiver)
            } else {
                cm.registerNetworkCallback(networkRequest, receiver)
            }
        }
        networkStateLiveDataMut.value = false
    }

    companion object {
        fun isNetworkAvailable(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            return activeNetwork?.isConnected ?: false
        }
    }
}