package com.juliusbaer.premarket.ui.phoneDialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juliusbaer.premarket.dataFlow.DataManger
import com.juliusbaer.premarket.dataFlow.IUserStorage
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.UserInfoModel
import com.juliusbaer.premarket.core.viewmodel.SingleLiveEvent
import kotlinx.coroutines.launch
import javax.inject.Inject

class PhoneDialogViewModel @Inject constructor(
        private val dataManager: DataManger,
        private val storage: IUserStorage
) : ViewModel() {
    private val phoneUpdateLiveDataMut = SingleLiveEvent<Resource<String>>()
    val phoneUpdateLiveData: LiveData<Resource<String>>
        get() = phoneUpdateLiveDataMut

    fun savePhoneNumber(number: String) {
        viewModelScope.launch {
            try {
                val userInfo = dataManager.getUserInfo()
                dataManager.putUserInfo(UserInfoModel(
                        userInfo.isNotificationForAlertsEnable,
                        userInfo.isNotificationForNewsEnable,
                        userInfo.underlyingDisplayingType,
                        userInfo.underlyingSortingOrder,
                        number))
                storage.savePhoneNumber(number)
                phoneUpdateLiveDataMut.value = Resource.success(number)
            } catch (throwable: Throwable) {
                phoneUpdateLiveDataMut.value = Resource.failure(throwable)
            }
        }
    }
}