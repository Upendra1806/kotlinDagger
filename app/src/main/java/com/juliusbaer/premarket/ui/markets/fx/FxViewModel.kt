package com.juliusbaer.premarket.ui.markets.fx

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.juliusbaer.premarket.dataFlow.IDataManager
import com.juliusbaer.premarket.dataFlow.database.IDbHelper
import com.juliusbaer.premarket.dataFlow.zeroMQ.ZeroMQHandler
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.FxBaseCurrency
import com.juliusbaer.premarket.models.serverModels.FxModel
import com.juliusbaer.premarket.models.serverModels.FxType
import com.juliusbaer.premarket.models.serverModels.ProductUpdateModel
import com.juliusbaer.premarket.ui.base.SocketVM
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class FxViewModel @Inject constructor(
        private val dataManager: IDataManager,
        private val database: IDbHelper,
        socket: ZeroMQHandler,
        gson: Gson): SocketVM(socket, gson) {

    private val itemsLiveDataMut = MutableLiveData<Resource<Pair<List<FxModel>, Boolean>>>()
    val itemsLiveData: LiveData<Resource<Pair<List<FxModel>, Boolean>>>
        get() = itemsLiveDataMut

    fun loadFxList(fxType: FxType, baseCurrency: FxBaseCurrency, scrollToTop: Boolean = false) {
        viewModelScope.launch {
            itemsLiveDataMut.value = try {
                val items = when (fxType) {
                    FxType.CURRENCY -> dataManager.getCurrencyPairs(baseCurrency)
                    FxType.METAL -> dataManager.getPreciousMetals(baseCurrency)
                }
                Resource.success(Pair(items, scrollToTop))
            } catch (throwable: Throwable) {
                Resource.failure(throwable, Pair(database.getFx(fxType, baseCurrency), scrollToTop))
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
}