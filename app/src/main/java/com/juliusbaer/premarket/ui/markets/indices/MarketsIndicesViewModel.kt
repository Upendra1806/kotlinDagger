package com.juliusbaer.premarket.ui.markets.indices

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.juliusbaer.premarket.dataFlow.IDataManager
import com.juliusbaer.premarket.dataFlow.database.IDbHelper
import com.juliusbaer.premarket.dataFlow.zeroMQ.ZeroMQHandler
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.IndexModel
import com.juliusbaer.premarket.models.serverModels.ProductUpdateModel
import com.juliusbaer.premarket.ui.base.SocketVM
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MarketsIndicesViewModel @Inject constructor(
        private val dataManager: IDataManager,
        private val database: IDbHelper,
        messageTask: ZeroMQHandler, gson: Gson): SocketVM(messageTask, gson) {

    private val itemsLiveDataMut = MutableLiveData<Resource<List<IndexModel>>>()
    val itemsLiveData: LiveData<Resource<List<IndexModel>>>
        get() = itemsLiveDataMut

    fun loadIndexes() {
        viewModelScope.launch {
            itemsLiveDataMut.value = try {
                Resource.success(dataManager.getIndexes())
            } catch (throwable: Throwable) {
                Resource.failure(throwable, database.getIndexes())
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