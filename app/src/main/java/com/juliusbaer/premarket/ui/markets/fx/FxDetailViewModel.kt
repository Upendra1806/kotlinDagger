package com.juliusbaer.premarket.ui.markets.fx

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.juliusbaer.premarket.dataFlow.IDataManager
import com.juliusbaer.premarket.dataFlow.IUserStorage
import com.juliusbaer.premarket.dataFlow.database.IDbHelper
import com.juliusbaer.premarket.dataFlow.zeroMQ.ZeroMQHandler
import com.juliusbaer.premarket.helpers.chart.ChartInterval
import com.juliusbaer.premarket.models.ChartData
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.FxModel
import com.juliusbaer.premarket.models.serverModels.FxType
import com.juliusbaer.premarket.ui.base.SocketVM
import com.juliusbaer.premarket.core.viewmodel.SingleLiveEvent
import kotlinx.coroutines.launch
import javax.inject.Inject

class FxDetailViewModel @Inject constructor(
        private val dataManager: IDataManager,
        private val database: IDbHelper,
        private val storage: IUserStorage,
        socket: ZeroMQHandler,
        gson: Gson): SocketVM(socket, gson) {

    private val itemLiveDataMut = MutableLiveData<Resource<FxModel>>()
    val itemLiveData: LiveData<Resource<FxModel>>
        get() = itemLiveDataMut

    private val watchListAddLiveDataMut = SingleLiveEvent<Resource<Int>>()
    val watchListAddLiveData: LiveData<Resource<Int>>
        get() = watchListAddLiveDataMut

    private val watchListDelLiveDataMut = SingleLiveEvent<Resource<Int>>()
    val watchListDelLiveData: LiveData<Resource<Int>>
        get() = watchListDelLiveDataMut

    fun isTokenValid(): Boolean = storage.getToken() != ""

    fun isConfirmed(): Boolean = storage.isConfirmed()

    fun loadFxModel(id: Int, type: FxType) {
        viewModelScope.launch {
            itemLiveDataMut.value = try {
                val result = when (type) {
                    FxType.CURRENCY -> dataManager.getCurrencyPair(id)
                    FxType.METAL -> dataManager.getPreciousMetal(id)
                }
                Resource.success(result)
            } catch (throwable: Throwable) {
                Resource.failure(throwable, database.getFxDetail(id, type))
            }
        }
    }

    fun deleteWatchListItemResponse(id: Int) {
        viewModelScope.launch {
            watchListDelLiveDataMut.value = try {
                dataManager.deleteWatchListItem(id)
                Resource.success(id)
            } catch (throwable: Throwable) {
                Resource.failure(throwable)
            }
        }
    }

    fun addWatchListItem(id: Int) {
        viewModelScope.launch {
            watchListAddLiveDataMut.value = try {
                dataManager.addWatchListItem(id)
                Resource.success(id)
            } catch (throwable: Throwable) {
                Resource.failure(throwable)
            }
        }
    }
}