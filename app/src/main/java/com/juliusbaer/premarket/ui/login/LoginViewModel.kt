package com.juliusbaer.premarket.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juliusbaer.premarket.dataFlow.AuthRepository
import com.juliusbaer.premarket.dataFlow.IUserStorage
import com.juliusbaer.premarket.models.responseModel.Resource
import kotlinx.coroutines.launch
import javax.inject.Inject


class LoginViewModel @Inject constructor(
        private val authRepository: AuthRepository,
        private val storage: IUserStorage
): ViewModel() {
    private val loginLiveDataMut = MutableLiveData<Resource<String>>()
    val loginLiveData: LiveData<Resource<String>>
        get() = loginLiveDataMut

    fun login(email: String) {
        viewModelScope.launch {
            loginLiveDataMut.value = try {
                val result = authRepository.registration(email)
                Resource.success(result.role)
            } catch (throwable: Throwable) {
                Resource.failure(throwable)
            }
        }
    }

    fun isConfirmed(): Boolean = storage.isConfirmed()
}