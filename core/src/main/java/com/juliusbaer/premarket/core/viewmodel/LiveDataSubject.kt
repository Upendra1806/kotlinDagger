package com.juliusbaer.premarket.core.viewmodel

import androidx.lifecycle.MutableLiveData


class LiveDataSubject<T>: MutableLiveData<T>() {
    override fun onActive() {
    }

    override fun onInactive() {
        value = null
    }
}