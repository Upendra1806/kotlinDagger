package com.juliusbaer.premarket.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juliusbaer.premarket.dataFlow.IDataManager
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.core.viewmodel.SingleLiveEvent
import kotlinx.coroutines.launch
import javax.inject.Inject


class AlertDeleteViewModel @Inject constructor(
      private val dataManager: IDataManager
): ViewModel() {
    val alertDel = SingleLiveEvent<Resource<Int>>()

    fun alertDelete(productId: Int) {
        viewModelScope.launch {
            alertDel.value = try {
                dataManager.deleteAlert(productId)
                Resource.success(productId)
            } catch (throwable: Throwable) {
                Resource.failure(throwable)
            }
        }
    }
}