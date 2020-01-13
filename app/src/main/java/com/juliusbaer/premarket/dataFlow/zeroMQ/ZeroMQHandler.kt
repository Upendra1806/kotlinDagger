package com.juliusbaer.premarket.dataFlow.zeroMQ

import androidx.annotation.WorkerThread
import com.juliusbaer.premarket.BuildConfig.SOCKET_BASE_URL
import com.juliusbaer.premarket.core.utils.CoroutinesDispatchers.IO
import com.juliusbaer.premarket.dataFlow.IUserStorage
import kotlinx.coroutines.withContext
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import zmq.io.mechanism.curve.Curve
import javax.inject.Inject

class ZeroMQHandler @Inject constructor(private val context: ZContext, private val storage: IUserStorage) {
    private var socket: ZMQ.Socket? = null
    private val topics = mutableListOf<String>()

    var isConnected = false
        private set

    @WorkerThread
    fun receiveString(): String? {
        val data: String? = socket?.recvStr(ZMQ.DONTWAIT)
        return if (!data.isNullOrEmpty()) {
            data
        } else {
            null
        }
    }

    fun clearTopics() {
        topics.clear()
    }

    @Synchronized
    suspend fun subscribeToTopics(newTopics: List<String>): Boolean {
        if (newTopics.isEmpty()) return false

        return withContext(IO) {
            if (socket == null) {
                socket = context.createSocket(SocketType.SUB)?.apply {
                    val curve = Curve()
                    val clientKeys = curve.keypairZ85()
                    curvePublicKey = clientKeys[0].toByteArray()
                    curveSecretKey = clientKeys[1].toByteArray()
                    curveServerKey = storage.getPublicKey().toByteArray()
                }
                isConnected = false
            }
            if (!isConnected) {
                isConnected = socket!!.connect(SOCKET_BASE_URL)
            }
            if (isConnected) {
                synchronized(topics) {
                    if (topics.isNotEmpty() && topics == newTopics) {
                        return@synchronized false
                    }
                    val subscribeTopics = newTopics.minus(topics)
                    if (subscribeTopics.isNotEmpty()) {
                        for (topic in subscribeTopics) {
                            socket?.subscribe(topic)
                        }
                        topics.addAll(subscribeTopics)
                        true
                    } else {
                        false
                    }
                }
            } else {
                false
            }
        }
    }

    suspend fun unsubscribeFromTopics(topics: List<String>) {
        if (socket != null && isConnected) {
            withContext(IO) {
                synchronized(this@ZeroMQHandler.topics) {
                    for (topic in topics) {
                        socket?.unsubscribe(topic)
                        this@ZeroMQHandler.topics.remove(topic)
                    }
                }
            }
        }
    }

    fun hasUnsubscribedTopics(): Boolean {
        return topics.isNotEmpty() && (socket == null || !isConnected)
    }

    suspend fun resubscribeToTopics() {
        val topicsCopy = topics.toList()
        topics.clear()

        subscribeToTopics(topicsCopy)
    }

    suspend fun close() {
        isConnected = false

        withContext(IO) {
            socket?.close()
            socket = null
        }
    }
}