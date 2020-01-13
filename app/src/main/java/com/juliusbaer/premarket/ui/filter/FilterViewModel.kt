package com.juliusbaer.premarket.ui.filter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juliusbaer.premarket.dataFlow.IDataManager
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.ExtremeWarrantModel
import com.juliusbaer.premarket.core.utils.CoroutinesDispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

class FilterViewModel @Inject constructor(
        private val dataManager: IDataManager): ViewModel() {

    private var findWarrantValuesJob: Job? = null

    private val allWarrantValuesLiveDataMut = MutableLiveData<Resource<ExtremeWarrantModel>>()
    val allWarrantValuesLiveData: LiveData<Resource<ExtremeWarrantModel>>
        get() = allWarrantValuesLiveDataMut

    private val productWarrantValuesLiveDataMut = MutableLiveData<Resource<ExtremeWarrantModel>>()
    val productWarrantValuesLiveData: LiveData<Resource<ExtremeWarrantModel>>
        get() = productWarrantValuesLiveDataMut

    private val underlyingsLiveDataMut = MutableLiveData<Resource<List<FilterSearchModel>>>()
    val underlyingsLiveData: LiveData<Resource<List<FilterSearchModel>>>
        get() = underlyingsLiveDataMut

    fun loadUnderlyings() {
        viewModelScope.launch {
            underlyingsLiveDataMut.value = try {
                val underlyingsResult = viewModelScope.async(IO) { dataManager.getUnderlyings() }
                val indexesResult = viewModelScope.async(IO) {  dataManager.getIndexes() }
                val searchableList = indexesResult.await().map { (FilterSearchModel(it.id, it.marketsTitle?:""))  }
                        .plus(underlyingsResult.await().map { (FilterSearchModel(it.id, it.title))  })
                Resource.success(searchableList)
            } catch (e: Throwable) {
                Resource.failure(e)
            }
        }
    }

    fun loadAllWarrantValues(id: Int?) {
        viewModelScope.launch {
            allWarrantValuesLiveDataMut.value = try {
                val result = dataManager.getExtremeWarrantValues()
                Resource.success(result)
            } catch (e: Throwable) {
                Resource.failure(e)
            }
            if (id != null) {
                productWarrantValuesLiveDataMut.value = try {
                    val result = dataManager.getExtremeWarrantValues(id)
                    Resource.success(result)
                } catch (e: Throwable) {
                    Resource.failure(e)
                }
            }
        }
    }

    fun loadProductWarrantValues(id: Int) {
        viewModelScope.launch {
            productWarrantValuesLiveDataMut.value = try {
                val result = dataManager.getExtremeWarrantValues(id)
                Resource.success(result)
            } catch (e: Throwable) {
                Resource.failure(e)
            }
        }
    }

    fun findExtremeWarrantValues(id: Int) {
        findWarrantValuesJob?.cancel()
        findWarrantValuesJob = viewModelScope.launch {
            productWarrantValuesLiveDataMut.value = try {
                val result = dataManager.getExtremeWarrantValues(id)
                Resource.success(result)
            } catch (e: Throwable) {
                Resource.failure(e)
            } finally {
                findWarrantValuesJob = null
            }
        }
    }

    fun cancelExtremeWarrantsRequest() {
        findWarrantValuesJob?.cancel()
    }
}