package com.juliusbaer.premarket.ui.underluings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.juliusbaer.premarket.dataFlow.IDataManager
import com.juliusbaer.premarket.dataFlow.database.IDbHelper
import com.juliusbaer.premarket.dataFlow.zeroMQ.ZeroMQHandler
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.*
import com.juliusbaer.premarket.ui.base.SocketVM
import com.juliusbaer.premarket.core.utils.CoroutinesDispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class UnderlyingListViewModel @Inject constructor(
        private val dataManager: IDataManager,
        private val database: IDbHelper,
        socket: ZeroMQHandler,
        gson: Gson) : SocketVM(socket, gson) {

    private val underlyingsLiveDataMut = MutableLiveData<Resource<List<UnderlyingModel>>>()
    val underlyingsLiveData: LiveData<Resource<List<UnderlyingModel>>>
        get() = underlyingsLiveDataMut

    private val candleSticksLiveDataMut = MutableLiveData<Resource<Pair<Int?, List<CandleStickModel>>>>()
    val candleSticksLiveData: LiveData<Resource<Pair<Int?, List<CandleStickModel>>>>
        get() = candleSticksLiveDataMut

    private val indexLiveDataMut = MutableLiveData<Resource<List<IndexModel>>>()
    val indexLiveData: LiveData<Resource<List<IndexModel>>>
        get() = indexLiveDataMut

    fun loadIndexAndCandlesSticks(type: UnderlyingType) {
        viewModelScope.launch {
            val indexesAsync = viewModelScope.async(IO) { dataManager.getIndexes(type) }
            val candlesAsync = viewModelScope.async(IO) { dataManager.getCandles(type) }
            val indexes = try {
                val indexes = indexesAsync.await()
                indexLiveDataMut.value = Resource.success(indexes)

                indexes
            } catch (throwable: Throwable) {
                val indexes = database.getIndexes(type)
                indexLiveDataMut.value = Resource.failure(throwable, indexes)
                indexes
            }
            val indexId = indexes.firstOrNull()?.id
            candleSticksLiveDataMut.value = try {
                val result = candlesAsync.await()
                Resource.success(Pair(indexId, result))
            } catch (throwable: Throwable) {
                Resource.failure(throwable, Pair(indexId, database.getCandles(type)))
            }
        }
    }

    fun loadUnderlyings(type: UnderlyingType) {
        viewModelScope.launch {
            underlyingsLiveDataMut.value = try {
                val result = dataManager.getUnderlyings(type)
                Resource.success(result)
            } catch (throwable: Throwable) {
                Resource.failure(throwable, database.getUnderlyings(type))
            }
        }
    }

    fun refreshLiveData(isBoxes: Boolean) {
        if (isBoxes) {
            if (underlyingsLiveDataMut.value != null) {
                underlyingsLiveDataMut.value = underlyingsLiveDataMut.value
            }
        } else {
            if (candleSticksLiveDataMut.value != null) {
                candleSticksLiveDataMut.value = candleSticksLiveDataMut.value
            }
        }
    }

    fun updateUnderlying(socketModel: ProductUpdateModel) {
        viewModelScope.launch {
            try {
                database.updateUnderlyingFromSocketModel(socketModel)
            } catch (throwable: Throwable) {
                Timber.e(throwable)
            }
        }
    }

    fun updateCandleStick(socketModel: CandleStickUpdateModel, type: UnderlyingType) {
        viewModelScope.launch {
            try {
                database.updateCandleStickFromSocketModel(socketModel, type)
            } catch (throwable: Throwable) {
                Timber.e(throwable)
            }
        }
    }

    fun updateIndex(socketModel: ProductUpdateModel) {
        viewModelScope.launch {
            try {
                database.updateIndexFromSocketModel(socketModel)
            } catch (throwable: Throwable) {
                Timber.e(throwable)
            }
        }
    }
}