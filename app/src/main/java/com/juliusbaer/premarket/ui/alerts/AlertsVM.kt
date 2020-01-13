package com.juliusbaer.premarket.ui.alerts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juliusbaer.premarket.dataFlow.IDataManager
import com.juliusbaer.premarket.dataFlow.database.IDbHelper
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.AlertsModel
import com.juliusbaer.premarket.core.viewmodel.SingleLiveEvent
import kotlinx.coroutines.launch
import javax.inject.Inject

class AlertsVM @Inject constructor(
        private val dataManager: IDataManager,
        private val database: IDbHelper): ViewModel() {

    private val itemsLiveDataMut = MutableLiveData<Resource<List<AlertsModel>>>()
    val itemsLiveData: LiveData<Resource<List<AlertsModel>>>
        get() = itemsLiveDataMut

    val alertDel = SingleLiveEvent<Resource<Int>>()
    val alertDelAll = SingleLiveEvent<Resource<Unit>>()

    fun alertDeleteRequest(productId: Int) {
        viewModelScope.launch {
            alertDel.value = try {
                dataManager.deleteAlert(productId)
                Resource.success(productId)
            } catch (throwable: Throwable) {
                Resource.failure(throwable)
            }
        }
    }

    fun loadAlerts() {
        viewModelScope.launch {
            itemsLiveDataMut.value = try {
                Resource.success(dataManager.getAlerts())
            } catch (throwable: Throwable) {
                Resource.failure(throwable, database.getAlerts())
            }
        }
    }

    fun alertDeleteAll() {
        viewModelScope.launch {
            alertDelAll.value = try {
                dataManager.alertDeleteAll()
                Resource.success(Unit)
            } catch (e: Throwable) {
                Resource.failure(e)
            }
        }
    }
}
