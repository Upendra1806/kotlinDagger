package com.juliusbaer.premarket.ui.alerts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juliusbaer.premarket.dataFlow.IDataManager
import com.juliusbaer.premarket.models.ProductType
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.AlertItemModel
import com.juliusbaer.premarket.models.serverModels.AlertSendModel
import com.juliusbaer.premarket.core.utils.CoroutinesDispatchers.IO
import com.juliusbaer.premarket.core.viewmodel.SingleLiveEvent
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

class UnderlyingAlertsViewModel @Inject constructor(
        private val dataManger: IDataManager
) : ViewModel() {
    private val alertsLiveDataMut = MutableLiveData<Resource<List<AlertItemModel>>>()
    val alertsLiveData: LiveData<Resource<List<AlertItemModel>>>
        get() = alertsLiveDataMut

    val setAlertLiveData = SingleLiveEvent<Resource<Unit>>()

    private val productLiveDataMut = MutableLiveData<Resource<AlertItem>>()
    val productLiveData: LiveData<Resource<AlertItem>>
        get() = productLiveDataMut

    fun loadProductAndAlerts(id: Int, type: ProductType) {
        viewModelScope.launch {
            val alertsAsync = viewModelScope.async(IO) { dataManger.getAlert(id) }
            when (type) {
                ProductType.UNDERLYING -> {
                    val underlyingAsync = viewModelScope.async(IO) { dataManger.getUnderlying(id) }
                    try {
                        val item = underlyingAsync.await()
                        productLiveDataMut.value = Resource.success(AlertItem.Underlying(item))
                    } catch (e: Throwable) {
                        productLiveDataMut.value = Resource.failure(e)
                        return@launch
                    }
                }
                ProductType.INDEX -> {
                    val indexAsync = viewModelScope.async(IO) { dataManger.getIndex(id) }
                    try {
                        val item = indexAsync.await()
                        productLiveDataMut.value = Resource.success(AlertItem.Index(item))
                    } catch (e: Throwable) {
                        productLiveDataMut.value = Resource.failure(e)
                        return@launch
                    }
                }
                ProductType.WARRANT -> {
                    val warrantAsync = viewModelScope.async(IO) { dataManger.getWarrant(id) }
                    try {
                        val item = warrantAsync.await()
                        productLiveDataMut.value = Resource.success(AlertItem.Warrant(item))
                    } catch (e: Throwable) {
                        productLiveDataMut.value = Resource.failure(e)
                        return@launch
                    }
                }
                ProductType.CURRENCY -> {
                    val fxAsync = viewModelScope.async(IO) { dataManger.getCurrencyPair(id) }
                    try {
                        val item = fxAsync.await()
                        productLiveDataMut.value = Resource.success(AlertItem.Fx(item))
                    } catch (e: Throwable) {
                        productLiveDataMut.value = Resource.failure(e)
                        return@launch
                    }
                }
                ProductType.METAL -> {
                    val fxAsync = viewModelScope.async(IO) { dataManger.getPreciousMetal(id) }
                    try {
                        val item = fxAsync.await()
                        productLiveDataMut.value = Resource.success(AlertItem.Fx(item))
                    } catch (e: Throwable) {
                        productLiveDataMut.value = Resource.failure(e)
                        return@launch
                    }
                }
            }
            alertsLiveDataMut.value = try {
                Resource.success(alertsAsync.await())
            } catch (e: Throwable) {
                Resource.failure(e)
            }
        }
    }

    fun setAlert(productId: Int, alerts: List<AlertSendModel>) {
        viewModelScope.launch {
            setAlertLiveData.value = try {
                if (alerts.isNotEmpty()) {
                    dataManger.setAlert(productId, alerts)
                } else {
                    dataManger.deleteAlert(productId)
                }
                Resource.success(Unit)
            } catch (e: Throwable) {
                Resource.failure(e)
            }
        }
    }
}

