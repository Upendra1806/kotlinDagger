package com.juliusbaer.premarket.ui.warrants

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.juliusbaer.premarket.dataFlow.IDataManager
import com.juliusbaer.premarket.dataFlow.database.IDbHelper
import com.juliusbaer.premarket.models.FilterModel
import com.juliusbaer.premarket.models.serverModels.WarrantModel
import kotlinx.coroutines.runBlocking
import retrofit2.HttpException
import java.net.HttpURLConnection
import java.util.concurrent.Executor
import javax.inject.Inject


class WarrantsVM @Inject constructor(
        private val dataManager: IDataManager,
        private val database: IDbHelper) : ViewModel() {

    var filter: FilterModel? = null
    var id: Int? = null

    private val networkStatusLiveDataMut = MutableLiveData<Throwable?>()
    val networkStatusLiveData: LiveData<Throwable?>
        get() = networkStatusLiveDataMut

    private val filterLiveDataMut = MutableLiveData<FilterModel?>()
    val filterLiveData: LiveData<FilterModel?>
        get() = filterLiveDataMut

    private val fetchExecutor = AsyncTask.THREAD_POOL_EXECUTOR

    private var resetFilter = false

    val warrantsLiveData =
            object: DataSource.Factory<Int, WarrantModel>() {
                override fun create(): DataSource<Int, WarrantModel> {
                    return WarrantsDataSource(fetchExecutor)
                }
            }.toLiveData(PagedList.Config.Builder()
                    .setEnablePlaceholders(false)
                    .setPageSize(30)
                    .setInitialLoadSizeHint(30)
                    .build(), fetchExecutor = fetchExecutor)

    fun retry() {
        (warrantsLiveData.value?.dataSource as? WarrantsDataSource)?.retryAllFailed()
    }

    private inner class WarrantsDataSource(private val executor: Executor): PageKeyedDataSource<Int, WarrantModel>() {
        private var retry: (() -> Any)? = null

        fun retryAllFailed() {
            val prevRetry = retry
            retry = null
            prevRetry?.let {
                executor.execute {
                    it.invoke()
                }
            }
        }

        override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, WarrantModel>) {
            runBlocking {
                if (filter == null && filterLiveData.value == null && !resetFilter) {
                    filter = try {
                        val filter = dataManager.getUserWarrantsFilter(id)
                        this@WarrantsVM.filter = filter
                        filterLiveDataMut.postValue(filter)
                        filter
                    } catch (throwable: Throwable) {
                        if (throwable is HttpException && throwable.code() == HttpURLConnection.HTTP_NOT_FOUND) {
                            this@WarrantsVM.filter = null
                            filterLiveDataMut.postValue(null)
                            null
                        } else {
                            retry = ::invalidate
                            networkStatusLiveDataMut.postValue(throwable)
                            return@runBlocking
                        }
                    }
                }
                try {
                    val result = dataManager.getWarrants(filter, id, 0, params.requestedLoadSize)
                    val nextPage = if (result.size < params.requestedLoadSize) null else 1
                    retry = null
                    callback.onResult(result, null, nextPage)
                    networkStatusLiveDataMut.postValue(null)

                    resetFilter = false
                } catch (throwable: Throwable) {
                    retry = ::invalidate
                    //callback.onResult(database.getWarrants(id), null, null)
                    networkStatusLiveDataMut.postValue(throwable)
                }
            }
        }

        override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, WarrantModel>) {
            if (networkStatusLiveDataMut.value == null) {
                runBlocking {
                    try {
                        val result = dataManager.getWarrants(filter, id, params.key, params.requestedLoadSize)
                        val nextPage = if (result.size < params.requestedLoadSize) null else params.key + 1
                        retry = null
                        callback.onResult(result, nextPage)
                        networkStatusLiveDataMut.postValue(null)
                    } catch (throwable: Throwable) {
                        retry = {
                            loadAfter(params, callback)
                        }
                        networkStatusLiveDataMut.postValue(throwable)
                    }
                }
            }
        }

        override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, WarrantModel>) {
        }
    }

    private fun invalidate() {
        warrantsLiveData.value?.dataSource?.invalidate()
    }

    fun updateFilter(filter: FilterModel?) {
        this.filter = filter
        filterLiveDataMut.value = filter

        if (filter == null) resetFilter = true
        invalidate()
    }
}