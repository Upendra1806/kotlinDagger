package com.juliusbaer.premarket.ui.underluings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.juliusbaer.premarket.dataFlow.IUserStorage
import javax.inject.Inject

class UnderlyingsViewModel @Inject constructor(
        private val storage: IUserStorage): ViewModel() {

    val boxesLiveData = MutableLiveData<Boolean>().apply {
        value = storage.getBoxes()
    }
    private val topLiveDataMut = MutableLiveData<Boolean>().apply {
        value = storage.getTop()
    }
    val topLiveData: LiveData<Boolean>
        get() = topLiveDataMut

    fun setBoxes(toBoolean: Boolean) {
        boxesLiveData.value = toBoolean
        storage.setBoxes(toBoolean)
    }

    fun setTop(value: Boolean) {
        storage.setTop(value)
        storage.setAlphabetic(!value)
        topLiveDataMut.value = value
    }
}
