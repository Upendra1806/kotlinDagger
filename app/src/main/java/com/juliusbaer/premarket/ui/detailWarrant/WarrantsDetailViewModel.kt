package com.juliusbaer.premarket.ui.detailWarrant

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.juliusbaer.premarket.dataFlow.IDataManager
import com.juliusbaer.premarket.dataFlow.IUserStorage
import com.juliusbaer.premarket.dataFlow.database.IDbHelper
import com.juliusbaer.premarket.dataFlow.zeroMQ.ZeroMQHandler
import com.juliusbaer.premarket.models.DetailWarrModel
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.ProductUpdateModel
import com.juliusbaer.premarket.ui.base.SocketVM
import com.juliusbaer.premarket.core.viewmodel.SingleLiveEvent
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class WarrantsDetailViewModel @Inject constructor(
        private val dataManager: IDataManager,
        private val database: IDbHelper,
        private val storage: IUserStorage,
        socket: ZeroMQHandler,
        gson: Gson): SocketVM(socket, gson) {

    private val itemLiveDataMut = MutableLiveData<Resource<DetailWarrModel>>()
    val itemLiveData: LiveData<Resource<DetailWarrModel>>
        get() = itemLiveDataMut

    private val watchListItemAddMut = SingleLiveEvent<Resource<Int>>()
    val watchListItemAdd: LiveData<Resource<Int>>
        get() = watchListItemAddMut

    private val watchListItemDelMut = SingleLiveEvent<Resource<Int>>()
    val watchListItemDel: LiveData<Resource<Int>>
        get() = watchListItemDelMut

    fun getDetailWarrant(id: Int){
        viewModelScope.launch {
            itemLiveDataMut.value = try {
                val result = dataManager.getWarrant(id)
                Resource.success(result)
            } catch (throwable: Throwable) {
                Resource.failure(throwable, database.getWarrantDetail(id))
            }
        }
    }

    fun isTokenValid(): Boolean = storage.getToken() != ""

    fun addWatchListItem(id: Int) {
        viewModelScope.launch {
            watchListItemAddMut.value = try {
                dataManager.addWatchListItem(id)
                Resource.success(id)
            } catch (throwable: Throwable) {
                Resource.failure(throwable)
            }
        }
    }

    fun isConfirmed(): Boolean = storage.isConfirmed()

    fun deleteWatchListItemResponse(id: Int) {
        viewModelScope.launch {
            watchListItemDelMut.value = try {
                dataManager.deleteWatchListItem(id)
                Resource.success(id)
            } catch (throwable: Throwable) {
                Resource.failure(throwable)
            }
        }
    }

    fun updateWarrant(product: ProductUpdateModel) {
        viewModelScope.launch {
            try {
                database.updateWarrantFromSocketModel(product)
            } catch (throwable: Throwable) {
                Timber.e(throwable)
            }
        }
    }
}