package com.juliusbaer.premarket.ui.watchlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.juliusbaer.premarket.core.utils.CoroutinesDispatchers
import com.juliusbaer.premarket.core.viewmodel.SingleLiveEvent
import com.juliusbaer.premarket.dataFlow.IDataManager
import com.juliusbaer.premarket.dataFlow.database.IDbHelper
import com.juliusbaer.premarket.dataFlow.zeroMQ.ZeroMQHandler
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.ProductUpdateModel
import com.juliusbaer.premarket.ui.base.SocketVM
import com.juliusbaer.premarket.ui.filter.FilterSearchModel
import com.juliusbaer.premarket.utils.Constants
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class WatchlistVM @Inject constructor(
        private val dataManager: IDataManager,
        private val database: IDbHelper,
        messageTask: ZeroMQHandler,
        gson: Gson) : SocketVM(messageTask, gson) {
    private val itemLiveDataMut = MutableLiveData<Resource<List<List<WatchListItem>>>>()
    val itemLiveData: LiveData<Resource<List<List<WatchListItem>>>>
        get() = itemLiveDataMut
    private val underlyingsLiveDataMut = MutableLiveData<Resource<List<WatchListSearchModel>>>()
    val underlyingsLiveData: LiveData<Resource<List<WatchListSearchModel>>>
        get() = underlyingsLiveDataMut

    val watchListItemDel = SingleLiveEvent<Resource<Int>>()

    fun getWatchListAnswer() {
        viewModelScope.launch {
            itemLiveDataMut.value = try {
                val result = dataManager.getWatchList()
                val groups = listOf(
                        result.underlyings?.map { WatchListItem.Underlying(it) }?.sortedBy { it.title }
                                ?: emptyList(),
                        result.warrants?.map { WatchListItem.Warrant(it) }?.sortedBy { it.title }
                                ?: emptyList(),
                        result.index?.map { WatchListItem.Index(it) }?.sortedBy { it.title }
                                ?: emptyList(),
                        result.currencyPairs?.map { WatchListItem.Fx(it) }?.sortedBy { it.title }
                                ?: emptyList(),
                        result.preciousMetals?.map { WatchListItem.Fx(it) }?.sortedBy { it.title }
                                ?: emptyList()
                )
                Resource.success(groups)
            } catch (throwable: Throwable) {
                Resource.failure(throwable)
            }
        }
    }

    fun deleteWatchListItem(id: Int) {
        viewModelScope.launch {
            watchListItemDel.value = try {
                dataManager.deleteWatchListItem(id)

                unsubscribeFromTopics(listOf(id))

                (itemLiveDataMut.value as? Resource.Success)?.data?.let { groups ->
                    for ((i, group) in groups.withIndex()) {
                        val pos = group.indexOfFirst { it.id == id }
                        if (pos >= 0) {
                            val groupMut = group.toMutableList().apply { removeAt(pos) }
                            val groupsUpd = groups.toMutableList().apply {
                                set(i, groupMut)
                            }
                            itemLiveDataMut.value = Resource.success(groupsUpd)
                        }
                    }
                }
                Resource.success(id)
            } catch (throwable: Throwable) {
                Resource.failure(throwable)
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

    fun updateIndex(socketModel: ProductUpdateModel) {
        viewModelScope.launch {
            try {
                database.updateIndexFromSocketModel(socketModel)
            } catch (throwable: Throwable) {
                Timber.e(throwable)
            }
        }
    }

    fun updateFxItem(socketModel: ProductUpdateModel) {
        viewModelScope.launch {
            try {
                database.updateFxItemFromSocketModel(socketModel)
            } catch (throwable: Throwable) {
                Timber.e(throwable)
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

    fun loadUnderlyings() {
        viewModelScope.launch {
            underlyingsLiveDataMut.value = try {
                val watchListResult = viewModelScope.async(CoroutinesDispatchers.IO) {dataManager.getWatchList()  }
                val underlyingsResult = viewModelScope.async(CoroutinesDispatchers.IO) { dataManager.getUnderlyings() }
                val indexesResult = viewModelScope.async(CoroutinesDispatchers.IO) {  dataManager.getIndexes() }

                val searchableList = indexesResult.await().map { (WatchListSearchModel(it.id, it.marketsTitle?:"",
                        Constants.ElementType.INDEX,it as Object))  }
                        .plus(underlyingsResult.await().map { (WatchListSearchModel(it.id, it.title,Constants.ElementType.UNDERLYING,it as Object))  })
                Resource.success(searchableList)
            } catch (e: Throwable) {
                Resource.failure(e)
            }
        }
    }
}
