package com.juliusbaer.premarket.ui.disclaimer

import androidx.lifecycle.ViewModel
import com.juliusbaer.premarket.dataFlow.IUserStorage
import javax.inject.Inject

class DisclaimerViewModel @Inject constructor(
        private val storage: IUserStorage
): ViewModel() {
    fun setFirstTimeValue() {
        storage.setFirstTimeLoading(false)
    }
}