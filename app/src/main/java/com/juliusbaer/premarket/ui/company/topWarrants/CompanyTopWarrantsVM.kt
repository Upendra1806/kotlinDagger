package com.juliusbaer.premarket.ui.company.topWarrants

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juliusbaer.premarket.dataFlow.IDataManager
import com.juliusbaer.premarket.dataFlow.database.IDbHelper
import com.juliusbaer.premarket.models.FilterModel
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.WarrantModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class CompanyTopWarrantsVM @Inject constructor(
        private val dataManager: IDataManager,
        private val database: IDbHelper) : ViewModel() {
    private val itemsLiveDataMut = MutableLiveData<Resource<List<WarrantModel>>>()
    val itemsLiveData: LiveData<Resource<List<WarrantModel>>>
        get() = itemsLiveDataMut

    fun getWarrantsTop(id: Int) {
        viewModelScope.launch {
            itemsLiveDataMut.value = try {
                val result = dataManager.getWarrants(FilterModel(topOnly = true), id, 0, Int.MAX_VALUE)
                Resource.success(result)
            } catch (throwable: Throwable) {
                Resource.failure(throwable, database.getWarrants(id, isTop = true))
            }
        }
    }
}
