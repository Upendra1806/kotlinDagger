package com.juliusbaer.premarket.ui.news

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juliusbaer.premarket.dataFlow.IDataManager
import com.juliusbaer.premarket.dataFlow.database.IDbHelper
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.NewsModel
import com.juliusbaer.premarket.core.viewmodel.SingleLiveEvent
import kotlinx.coroutines.launch
import javax.inject.Inject

class NewsVM @Inject constructor(
        private val dataManager: IDataManager,
        private val database: IDbHelper) : ViewModel() {

    val delNew = SingleLiveEvent<Resource<Int>>()
    private val itemsLiveDataMut = MutableLiveData<Resource<List<NewsModel>>>()
    val itemsLiveData: LiveData<Resource<List<NewsModel>>>
        get() = itemsLiveDataMut

    val newsLiveData: LiveData<List<Int>?> = dataManager.newsLiveData

    fun newsDel(id: Int) {
        viewModelScope.launch {
            delNew.value = try {
                dataManager.newsDel(id)
                Resource.success(id)
            } catch (throwable: Throwable) {
                Resource.failure(throwable)
            }
        }
    }

    fun loadNews() {
        viewModelScope.launch {
            itemsLiveDataMut.value = try {
                Resource.success(dataManager.getNews())
            } catch (throwable: Throwable) {
                Resource.failure(throwable, database.getNews())
            }
        }
    }
}