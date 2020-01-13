package com.juliusbaer.premarket.ui.main

import androidx.lifecycle.ViewModel
import com.juliusbaer.premarket.dataFlow.IUserStorage
import javax.inject.Inject

class HomeViewModel @Inject constructor(
        private val storage: IUserStorage): ViewModel() {
    fun isConfirmed(): Boolean = storage.isConfirmed()
}
