package com.juliusbaer.premarket.ui.promotion

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juliusbaer.premarket.dataFlow.IDataManager
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.PromotionInfoModel
import kotlinx.coroutines.launch
import javax.inject.Inject


class PromotionViewModel @Inject constructor(
        private val dataManager: IDataManager) : ViewModel() {
    private val promotionLiveDataMut = MutableLiveData<Resource<PromotionInfoModel?>>()
    val promotionLiveData: LiveData<Resource<PromotionInfoModel?>>
        get() = promotionLiveDataMut

    fun getPromotion() {
        viewModelScope.launch {
            promotionLiveDataMut.value = try {
                Resource.success(dataManager.getPromotionInfo())
            } catch (e: Throwable) {
                Resource.failure(e)
            }
        }
    }
}