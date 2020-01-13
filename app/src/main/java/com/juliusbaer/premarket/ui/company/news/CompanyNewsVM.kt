package com.juliusbaer.premarket.ui.company.news

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juliusbaer.premarket.core.viewmodel.SingleLiveEvent
import com.juliusbaer.premarket.dataFlow.IDataManager
import com.juliusbaer.premarket.dataFlow.database.IDbHelper
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.NewsModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class CompanyNewsVM @Inject constructor(
        private val dataManager: IDataManager,
        private val database: IDbHelper) : ViewModel() {
    private val itemsLiveDataMut = MutableLiveData<Resource<List<NewsModel>>>()
    val itemsLiveData: LiveData<Resource<List<NewsModel>>>
        get() = itemsLiveDataMut
    val delNew = SingleLiveEvent<Resource<Int>>()

    val newsLiveData: LiveData<List<Int>?> = dataManager.newsLiveData

    fun getNewsById(id: Int) {
        viewModelScope.launch {
            itemsLiveDataMut.value = try {
                val result = dataManager.getNewsByUnderlyingId(id)
                Resource.success(result)
            } catch (throwable: Throwable) {
                Resource.failure(throwable, database.getNews(id))
            }
        }
    }

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
}
