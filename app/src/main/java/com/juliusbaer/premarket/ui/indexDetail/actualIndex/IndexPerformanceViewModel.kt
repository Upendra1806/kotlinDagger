package com.juliusbaer.premarket.ui.indexDetail.actualIndex

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.juliusbaer.premarket.dataFlow.IDataManager
import com.juliusbaer.premarket.dataFlow.database.IDbHelper
import com.juliusbaer.premarket.dataFlow.zeroMQ.ZeroMQHandler
import com.juliusbaer.premarket.helpers.chart.ChartInterval
import com.juliusbaer.premarket.models.ChartData
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.IndexModel
import com.juliusbaer.premarket.models.serverModels.ProductUpdateModel
import com.juliusbaer.premarket.ui.base.SocketVM
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class IndexPerformanceViewModel @Inject constructor(
        private val dataManager: IDataManager,
        private val database: IDbHelper,
        socket: ZeroMQHandler,
        gson: Gson): SocketVM(socket, gson) {
    private val chartLiveDataMut = MutableLiveData<Resource<Pair<ChartData, ChartInterval>>>()
    val chartLiveData: LiveData<Resource<Pair<ChartData, ChartInterval>>>
        get() = chartLiveDataMut

    private val indexLiveDataMut = MutableLiveData<Resource<IndexModel>>()
    val indexLiveData: LiveData<Resource<IndexModel>>
        get() = indexLiveDataMut

    fun getChartDataResult(id: Int, period: ChartInterval) {
        viewModelScope.launch {
            chartLiveDataMut.value = try {
                val result = dataManager.getChartDataResult(id, period)
                Resource.success(Pair(result, period))
            } catch (throwable: Throwable) {
                val result = database.getChartData(id, period.v)
                Resource.failure(throwable, if (result != null) Pair(result, period) else null)
            }
        }
    }

    fun loadIndex(id: Int) {
        viewModelScope.launch {
            indexLiveDataMut.value = try {
                val result = dataManager.getIndex(id)
                Resource.success(result)
            } catch (throwable: Throwable) {
                Resource.failure(throwable, database.getIndex(id))
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