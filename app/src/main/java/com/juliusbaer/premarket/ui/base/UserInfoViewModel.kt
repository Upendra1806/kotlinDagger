package com.juliusbaer.premarket.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juliusbaer.premarket.dataFlow.IDataManager
import com.juliusbaer.premarket.dataFlow.IUserStorage
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.UserInfoModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserInfoViewModel @Inject constructor(
        private val dataManager: IDataManager,
        private val storage: IUserStorage) : ViewModel() {

    private val userInfoLiveDataMut = MutableLiveData<Resource<UserInfoModel>>()
    val userInfoLiveData: LiveData<Resource<UserInfoModel>>
        get() = userInfoLiveDataMut

    fun syncUserInfo() {
        viewModelScope.launch {
            userInfoLiveDataMut.value = try {
                val result = dataManager.getUserInfo()
                storage.savePhoneNumber(result.traderPhoneNumber ?: "")
                Resource.success(result)
            } catch (throwable: Throwable) {
                Resource.failure(throwable)
            }
        }
    }
}