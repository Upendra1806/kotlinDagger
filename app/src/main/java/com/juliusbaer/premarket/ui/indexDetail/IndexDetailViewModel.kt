package com.juliusbaer.premarket.ui.indexDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juliusbaer.premarket.dataFlow.IDataManager
import com.juliusbaer.premarket.dataFlow.IUserStorage
import com.juliusbaer.premarket.dataFlow.database.IDbHelper
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.IndexModel
import com.juliusbaer.premarket.core.viewmodel.SingleLiveEvent
import kotlinx.coroutines.launch
import javax.inject.Inject

class IndexDetailViewModel @Inject constructor(
        private val dataManager: IDataManager,
        private val database: IDbHelper,
        private val storage: IUserStorage): ViewModel() {

    private val indexLiveDataMut = MutableLiveData<Resource<IndexModel>>()
    val indexLiveData: LiveData<Resource<IndexModel>>
        get() = indexLiveDataMut

    private val watchListItemAddMut = SingleLiveEvent<Resource<Int>>()
    val watchListItemAdd: LiveData<Resource<Int>>
        get() = watchListItemAddMut

    private val watchListItemDelMut = SingleLiveEvent<Resource<Int>>()
    val watchListItemDel: LiveData<Resource<Int>>
        get() = watchListItemDelMut

    fun isConfirmed(): Boolean = storage.isConfirmed()

    fun isTokenValid(): Boolean = storage.getToken() != ""

    fun loadIndex(id: Int) {
        viewModelScope.launch {
            indexLiveDataMut.value = try {
                val result = dataManager.getIndex(id)
                Resource.success(result)
            } catch (throwable: Throwable) {
                Resource.failure(throwable, database.getIndex(id))
            }
        }
    }

    fun addWatchListItem(id: Int) {
        viewModelScope.launch {
            watchListItemAddMut.value = try {
                dataManager.addWatchListItem(id)
                Resource.success(id)
            } catch (throwable: Throwable) {
                Resource.failure(throwable)
            }
        }
    }

    fun deleteWatchListItemResponse(id: Int) {
        viewModelScope.launch {
            watchListItemDelMut.value = try {
                dataManager.deleteWatchListItem(id)
                Resource.success(id)
            } catch (throwable: Throwable) {
                Resource.failure(throwable)
            }
        }
    }
}