package com.juliusbaer.premarket.ui.company

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.juliusbaer.premarket.dataFlow.IDataManager
import com.juliusbaer.premarket.dataFlow.IUserStorage
import com.juliusbaer.premarket.dataFlow.database.IDbHelper
import com.juliusbaer.premarket.dataFlow.zeroMQ.ZeroMQHandler
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.UnderlyingModel
import com.juliusbaer.premarket.ui.base.SocketVM
import com.juliusbaer.premarket.core.viewmodel.SingleLiveEvent
import kotlinx.coroutines.launch
import javax.inject.Inject

class CompanyVM @Inject constructor(
        private val dataManager: IDataManager,
        private val database: IDbHelper,
        private val storage: IUserStorage,
        socket: ZeroMQHandler,
        gson: Gson): SocketVM(socket, gson) {
    private val watchListAddLiveDataMut = SingleLiveEvent<Resource<Int>>()
    val watchListAddLiveData: LiveData<Resource<Int>>
        get() = watchListAddLiveDataMut

    private val watchListDelLiveDataMut = SingleLiveEvent<Resource<Int>>()
    val watchListDelLiveData: LiveData<Resource<Int>>
        get() = watchListDelLiveDataMut

    private val itemLiveDataMut =  MutableLiveData<Resource<UnderlyingModel>>()
    val itemLiveData: LiveData<Resource<UnderlyingModel>>
        get() = itemLiveDataMut

    fun isConfirmed(): Boolean = storage.isConfirmed()
    fun isTokenValid(): Boolean = storage.getToken() != ""

    fun loadUnderlyingItemById(id: Int) {
        viewModelScope.launch {
            itemLiveDataMut.value = try {
                Resource.success(dataManager.getUnderlying(id))
            } catch (throwable: Throwable) {
                Resource.failure(throwable, database.getUnderlying(id))
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
