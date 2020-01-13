package com.juliusbaer.premarket.ui.detailNews

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juliusbaer.premarket.dataFlow.IDataManager
import com.juliusbaer.premarket.dataFlow.database.IDbHelper
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.NewsModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class NewsDetailViewModel @Inject constructor(
        private val dataManager: IDataManager,
        private val database: IDbHelper): ViewModel() {
    private val itemLiveDataMut = MutableLiveData<Resource<NewsModel>>()
    val itemLiveData: LiveData<Resource<NewsModel>>
        get() = itemLiveDataMut

    fun loadNewsDetail(id: Int) {
        viewModelScope.launch {
            itemLiveDataMut.value = try {
                Resource.success(dataManager.getNewsById(id))
            } catch (e: Throwable) {
                Resource.failure(e, database.getNewsById(id))
            }
        }
    }
}
