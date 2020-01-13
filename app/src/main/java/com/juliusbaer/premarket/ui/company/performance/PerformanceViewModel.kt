package com.juliusbaer.premarket.ui.company.performance

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
import com.juliusbaer.premarket.models.serverModels.UnderlyingModel
import com.juliusbaer.premarket.ui.base.SocketVM
import kotlinx.coroutines.launch
import javax.inject.Inject

class PerformanceViewModel @Inject constructor(
        private val dataManager: IDataManager,
        private val database: IDbHelper,
        socket: ZeroMQHandler,
        gson: Gson): SocketVM(socket, gson) {
    private val chartLiveDataMut = MutableLiveData<Resource<Pair<ChartData, ChartInterval>>>()
    val chartLiveData: LiveData<Resource<Pair<ChartData, ChartInterval>>>
        get() = chartLiveDataMut

    private val itemLiveDataMut: MutableLiveData<Resource<UnderlyingModel>> = MutableLiveData()
    val itemLiveData: LiveData<Resource<UnderlyingModel>>
        get() = itemLiveDataMut

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

    fun loadUnderlying(id: Int) {
        viewModelScope.launch {
            itemLiveDataMut.value = try {
                Resource.success(dataManager.getUnderlying(id))
            } catch (throwable: Throwable) {
                Resource.failure(throwable, database.getUnderlying(id))
            }
        }
    }
}
