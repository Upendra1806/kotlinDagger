package com.juliusbaer.premarket.ui.mostActive

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juliusbaer.premarket.dataFlow.IDataManager
import com.juliusbaer.premarket.dataFlow.database.IDbHelper
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.WarrantModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class MostActiveViewModel @Inject constructor(
        private val dataManager: IDataManager,
        private val database: IDbHelper) : ViewModel() {

    private val itemsLiveDataMut = MutableLiveData<Resource<List<WarrantModel>>>()
    val itemsLiveData: LiveData<Resource<List<WarrantModel>>>
        get() = itemsLiveDataMut

    fun loadMostActive() {
        viewModelScope.launch {
            itemsLiveDataMut.value = try {
                Resource.success(dataManager.getMostActiveWarrants())
            } catch (throwable: Throwable) {
                Resource.failure(throwable, database.getWarrants(isMostActive = true))
            }
        }
    }
}
