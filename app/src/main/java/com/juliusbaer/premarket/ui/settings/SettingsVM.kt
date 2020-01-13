package com.juliusbaer.premarket.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juliusbaer.premarket.dataFlow.IDataManager
import com.juliusbaer.premarket.dataFlow.IUserStorage
import com.juliusbaer.premarket.helpers.chart.ChartInterval
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.UserInfoModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SettingsVM @Inject constructor(
        private val dataManager: IDataManager,
        private val storage: IUserStorage) : ViewModel() {

    private val userInfoUpdateEvent = MutableLiveData<Resource<SettingsOption>>()
    val userInfoUpdateLiveData: LiveData<Resource<SettingsOption>>
        get() = userInfoUpdateEvent

    private val userInfoLiveDataMut = MutableLiveData<Resource<UserInfoModel>>()
    val userInfoLiveData: LiveData<Resource<UserInfoModel>>
        get() = userInfoLiveDataMut

    private val isLogout: MutableLiveData<Boolean> = MutableLiveData()
    fun isLogout() = isLogout

    fun setIsConfirmed(isConfirmed: Boolean) {
        storage.setIsConfirmed(isConfirmed)
    }

    fun logout() {
        viewModelScope.launch {
            isLogout.value = try {
                dataManager.logout()
                setIsConfirmed(false)
                true
            } catch (e: Throwable) {
                Timber.e("Error logout ${e.message}")
                false
            }
        }
    }

    fun saveFxInterval(interval: ChartInterval) {
        storage.fxInterval = interval
    }

    fun saveInterval(interval: ChartInterval) {
        storage.interval = interval
    }

    fun getUserInfo() {
        viewModelScope.launch {
            userInfoLiveDataMut.value = try {
                val result = dataManager.getUserInfo()
                Resource.success(result)
            } catch (throwable: Throwable) {
                Resource.failure(throwable)
            }
        }
    }

    fun putUserInfoRequest(type: SettingsOption, switchAlertChecked: Boolean, switchNewsChecked: Boolean, underlyingDisplayingType: Int, UnderlyingSortingOrder: Int, phoneNumber: String) {
        viewModelScope.launch {
            userInfoUpdateEvent.value = try {
                dataManager.putUserInfo(UserInfoModel(switchAlertChecked, switchNewsChecked, underlyingDisplayingType, UnderlyingSortingOrder, phoneNumber))
                storage.savePhoneNumber(phoneNumber)

                Resource.success(type)
            } catch (throwable: Throwable) {
                Resource.failure(throwable)
            }
        }
    }

    enum class SettingsOption {
        PHONE,
        NEWS,
        NOTIFICATIONS
    }
}