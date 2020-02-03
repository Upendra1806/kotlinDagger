package com.juliusbaer.premarket.ui.chart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.juliusbaer.premarket.dataFlow.IDataManager
import com.juliusbaer.premarket.dataFlow.database.IDbHelper
import com.juliusbaer.premarket.dataFlow.zeroMQ.ZeroMQHandler
import com.juliusbaer.premarket.helpers.chart.ChartInterval
import com.juliusbaer.premarket.models.CandleStickModel.CandleChartData
import com.juliusbaer.premarket.models.ChartData
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.ui.base.SocketVM
import com.juliusbaer.premarket.utils.Constants
import kotlinx.coroutines.launch
import javax.inject.Inject

class BaseChartViewModel @Inject constructor(
        private val dataManager: IDataManager,
        private val database: IDbHelper, messageTask: ZeroMQHandler, gson: Gson) : SocketVM(messageTask,gson){

    private val candleChartLiveDataMut = MutableLiveData<Resource<Pair<CandleChartData, ChartInterval>>>()
    private  var chartTypeLiveData: MutableLiveData<String> = MutableLiveData(Constants.ChartConstant.LINE_CHART)
    val candleChartLiveData: LiveData<Resource<Pair<CandleChartData, ChartInterval>>>
        get() = candleChartLiveDataMut
    private val chartLiveDataMut = MutableLiveData<Resource<Pair<ChartData, ChartInterval>>>()
    val chartLiveData: LiveData<Resource<Pair<ChartData, ChartInterval>>>
        get() = chartLiveDataMut


    fun setChartType(type: String) {
        chartTypeLiveData.value = type
    }

    fun getChartType():MutableLiveData<String>
    {
        return chartTypeLiveData;
    }

    fun getCandleChartDataResult(id: Int, period: ChartInterval) {
        //TODO need to add network call and retrieve database query
    }

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



}