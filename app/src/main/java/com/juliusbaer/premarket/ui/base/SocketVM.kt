package com.juliusbaer.premarket.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.juliusbaer.premarket.core.viewmodel.SingleLiveEvent
import com.juliusbaer.premarket.dataFlow.zeroMQ.SubscriptionPrefix
import com.juliusbaer.premarket.dataFlow.zeroMQ.ZeroMQHandler
import com.juliusbaer.premarket.models.serverModels.CandleStickUpdateModel
import com.juliusbaer.premarket.models.serverModels.ProductUpdateModel
import com.juliusbaer.premarket.utils.Constants
import kotlinx.coroutines.*
import timber.log.Timber
import java.nio.channels.ClosedSelectorException

open class SocketVM(private val messageTask: ZeroMQHandler, private val gson: Gson): ViewModel() {
    private val productModelLiveDataMut = SingleLiveEvent<ProductUpdateModel>()
    val productModelLiveData: LiveData<ProductUpdateModel>
        get() = productModelLiveDataMut

    private val candleModelLiveDataMut = SingleLiveEvent<CandleStickUpdateModel>()
    val candleModelLiveData: LiveData<CandleStickUpdateModel>
        get() = candleModelLiveDataMut

    private var socketJob: Job? = null

    fun subscribeToRtUpdates(topics: List<Int>, subscriptionPrefix: SubscriptionPrefix = SubscriptionPrefix.PRODUCT) {
        viewModelScope.launch {
            val socketTopics = topics.map { String.format("%s%0${Constants.SOCKET_TOPIC_DIGITS}d", subscriptionPrefix.v, it) }
            try {
                if (messageTask.subscribeToTopics(socketTopics)) {
                    Timber.d("subscribeToRtUpdates $socketTopics %s", this@SocketVM.javaClass.simpleName)

                    subscribeToMessages()
                }
            } catch (throwable: Throwable) {
                Timber.e(throwable)
            }
        }
    }

    fun unsubscribeFromTopics(topics: List<Int>, subscriptionPrefix: SubscriptionPrefix = SubscriptionPrefix.PRODUCT) {
        val socketTopics = topics.map { String.format("%s%0${Constants.SOCKET_TOPIC_DIGITS}d", subscriptionPrefix.v, it) }
        viewModelScope.launch {
            try {
                messageTask.unsubscribeFromTopics(socketTopics)
            } catch (throwable: Throwable) {
                Timber.e(throwable)
            }
        }
    }

    fun resubscribeToRtUpdates() {
        if (messageTask.hasUnsubscribedTopics()) {
            viewModelScope.launch {
                Timber.d("resubscribeToRtUpdates %s", this@SocketVM.javaClass.simpleName)
                try {
                    messageTask.resubscribeToTopics()
                    subscribeToMessages()
                } catch (throwable: Throwable) {
                    Timber.e(throwable)
                }
            }
        }
    }

    fun unsubscribeFromRtUpdates(clearSavedTopics: Boolean = false) {
        Timber.d("unsubscribeFromRtUpdates clearSavedTopics($clearSavedTopics) %s", this.javaClass.simpleName)
        unsubscribeFromMessages()
        closeSocket()

        if (clearSavedTopics) messageTask.clearTopics()
    }

    private fun closeSocket() {
        if (messageTask.isConnected) {
            GlobalScope.launch {
                try {
                    messageTask.close()
                } catch (throwable: Throwable) {
                    Timber.e(throwable)
                }
            }
        }
    }

    fun clearTopics() {
        messageTask.clearTopics()
    }

    private fun subscribeToMessages() {
        if (socketJob != null) return

        Timber.d("subscribeToMessages %s", this.javaClass.simpleName)
        //use Dispatchers.IO instead if app's CoroutinesDispatchers.IO because espresso waits until
        //AsyncTask.THREAD_POOL_EXECUTOR (which overrides CoroutinesDispatchers.IO in tests) becomes idle and here we have infinite loop
        socketJob = viewModelScope.launch(Dispatchers.IO) {
            do {
                try {
                    val data = messageTask.receiveString()
                    if (!isActive) break

                    if (!data.isNullOrEmpty()) {
                        when {
                            data.startsWith(SubscriptionPrefix.PRODUCT.v) -> {
                                val productJson = messageTask.receiveString()
                                if (!isActive) return@launch
                                if (!productJson.isNullOrEmpty()) parseProduct(productJson)
                            }
                            data.startsWith(SubscriptionPrefix.CANDLESTICK.v) -> {
                                val candleJson = messageTask.receiveString()
                                if (!isActive) return@launch
                                if (!candleJson.isNullOrEmpty()) parseCandle(candleJson)
                            }
                            data.startsWith("{") -> {
                                try {
                                    parseProduct(data)
                                } catch (throwable: JsonParseException) {
                                    parseCandle(data)
                                }
                            }
                            else -> Timber.e("invalid real-time data $data")
                        }
                        delay(30)
                    }
                } catch (e: Throwable) {
                    if (e !is ClosedSelectorException) Timber.e(e)
                }
            } while (true)
        }
    }

    private fun parseProduct(data: String) {
        val product = gson.fromJson(data, ProductUpdateModel::class.java)
        productModelLiveDataMut.postValue(product)
    }

    private fun parseCandle(data: String) {
        val candle = gson.fromJson(data, CandleStickUpdateModel::class.java)
        candleModelLiveDataMut.postValue(candle)
    }

    private fun unsubscribeFromMessages() {
        socketJob?.cancel()
        socketJob = null
    }

    override fun onCleared() {
        unsubscribeFromMessages()
        closeSocket()
        super.onCleared()
    }
}