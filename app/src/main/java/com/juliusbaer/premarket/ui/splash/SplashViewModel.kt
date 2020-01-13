package com.juliusbaer.premarket.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juliusbaer.premarket.dataFlow.AuthRepository
import com.juliusbaer.premarket.dataFlow.IUserStorage
import com.juliusbaer.premarket.models.responseModel.Resource
import kotlinx.coroutines.launch
import javax.inject.Inject

class SplashViewModel @Inject constructor(
        private val authRepository: AuthRepository,
        private val storage: IUserStorage) : ViewModel() {
    private val authLiveDataMut = MutableLiveData<Resource<Boolean>>()
    val authLiveData: LiveData<Resource<Boolean>>
        get() = authLiveDataMut

    fun auth() {
        viewModelScope.launch {
            authLiveDataMut.value = try {
                authRepository.registration(storage.getEmail())
                Resource.success(true)
            } catch (throwable: Throwable) {
                Resource.failure(throwable)
            }
        }
    }

    fun setUserId(deviceId: String) {
        storage.setUserId(deviceId)
    }
}