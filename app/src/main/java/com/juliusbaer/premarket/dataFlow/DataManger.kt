package com.juliusbaer.premarket.dataFlow

import com.juliusbaer.premarket.dataFlow.database.IDbHelper
import com.juliusbaer.premarket.dataFlow.network.Api
import com.juliusbaer.premarket.helpers.chart.ChartInterval
import com.juliusbaer.premarket.models.ChartData
import com.juliusbaer.premarket.models.DetailWarrModel
import com.juliusbaer.premarket.models.FilterModel
import com.juliusbaer.premarket.models.serverModels.*
import com.juliusbaer.premarket.core.utils.CoroutinesDispatchers.IO
import com.juliusbaer.premarket.core.viewmodel.SingleLiveEvent
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DataManger @Inject constructor(private val api: Api, private val storage: IUserStorage, private val database: IDbHelper) : IDataManager {
    override val newsLiveData = SingleLiveEvent<List<Int>?>()

    override suspend fun getMostActiveWarrants(): List<WarrantModel> {
        return withContext(IO) {
            val items = api.getMostActive()
            database.saveWarrants(items, isMostActive = true)
            items
        }
    }

    override suspend fun getExtremeWarrantValues(productId: Int?): ExtremeWarrantModel {
        return withContext(IO) {
            api.getExtremeWarrantValues(productId)
        }
    }

    override suspend fun newsDel(id: Int) {
        withContext(IO) {
            api.newsDel(id)
        }
    }

    override suspend fun getWarrants(filterModel: FilterModel?, id: Int?, pageNumber: Int, pageSize: Int): List<WarrantModel> {
        return withContext(IO) {
            val items = api.getWarrants(
                    id == null,
                    filterModel?.underlyingId ?: id,
                    filterModel?.contractOption,
                    filterModel?.maturityStartDate,
                    filterModel?.maturityEndDate,
                    filterModel?.strikePriceMin,
                    filterModel?.strikePriceMax,
                    filterModel?.topOnly,
                    filterModel?.category,
                    filterModel?.tradedVolume,
                    pageNumber,
                    pageSize)
            database.saveWarrants(items, id)
            items
        }
    }

    override suspend fun getNewsByUnderlyingId(id: Int): List<NewsModel> {
        return withContext(IO) {
            val items = api.getNewsByUnderlyingId(listOf(id))
            database.saveNews(items, id)
            items
        }
    }

    override suspend fun getChartDataResult(id: Int, period: ChartInterval): ChartData {
        return withContext(IO) {
            val data = api.getChartDataResult(id, period.v)
            database.saveChartData(id, period, data)
            data
        }
    }

    override suspend fun deleteWatchListItem(id: Int) {
        withContext(IO) {
            api.deleteWatchListItem(id)
        }
    }

    override suspend fun addWatchListItem(id: Int) {
        withContext(IO) {
            api.addWatchListItem(id)
        }
    }

    override suspend fun getIndex(id: Int): IndexModel {
        return withContext(IO) {
            val index = api.getIndex(id)
            database.saveIndexDetail(index)
            index
        }
    }

    override suspend fun alertDeleteAll() {
        withContext(IO) {
            api.alertDeleteAll()
        }
    }

    override suspend fun getCandles(type: UnderlyingType): List<CandleStickModel> {
        val items = when (type) {
            UnderlyingType.SMI -> api.getCandleSMI()
            UnderlyingType.MIDCAP -> api.getCandleMidCap()
        }
        database.saveCandles(items, type)
        return items
    }

    override suspend fun getPromotionInfo(): PromotionInfoModel? {
        return withContext(IO) {
            val promotionInfo = api.getPromotionInfo().await()
            if (promotionInfo != null) {
                if (storage.promotionLastModifiedDate != promotionInfo.lastModifiedDate) {
                    storage.promotionLastModifiedDate = promotionInfo.lastModifiedDate

                    promotionInfo
                } else {
                    null
                }
            } else {
                null
            }
        }

    }

    override suspend fun logout() {
        withContext(IO) {
            api.logout()
        }
    }

    override suspend fun getAlert(id: Int) = api.getAlert(id)

    override suspend fun getNewsById(id: Int): NewsModel {
        return withContext(IO) {
            api.getNewsById(id)
        }
    }

    override suspend fun getNews(): List<NewsModel> {
        return withContext(IO) {
            val items = api.getNews()
            database.saveNews(items)
            items
        }
    }

    override suspend fun putUserInfo(userInfoModel: UserInfoModel) {
        withContext(IO) {
            api.putUserProfile(userInfoModel)
        }
    }

    override suspend fun getUserInfo(): UserInfoModel {
        return withContext(IO) {
            api.getUserProfile()
        }
    }

    override suspend fun setAlert(productId: Int, alertSendModel: List<AlertSendModel>) {
        withContext(IO) {
            api.sendAlerts(productId, alertSendModel)
        }
    }

    override suspend fun deleteAlert(productId: Int) {
        withContext(IO) {
            api.deleteAlerts(productId)
        }
    }

    override suspend fun getAlerts(): List<AlertsModel> {
        return withContext(IO) {
            val items = api.getAlerts()
            database.saveAlerts(items)
            items
        }
    }

    override suspend fun getWatchList(): WatchlistInfoModel {
        return withContext(IO) {
            val items = api.getWatchList()
            items.currencyPairs?.forEach { it.type = FxType.CURRENCY.ordinal }
            items.preciousMetals?.forEach { it.type = FxType.METAL.ordinal }
            items
        }
    }

    override suspend fun getWarrant(id: Int): DetailWarrModel {
        return withContext(IO) {
            val item = api.getWarrantById(id)
            database.saveWarrant(item)
            item
        }
    }

    override suspend fun getIndexes(type: UnderlyingType?): List<IndexModel> {
        return withContext(IO) {
            val indexes = when (type) {
                UnderlyingType.SMI -> api.getSmiIndex()
                UnderlyingType.MIDCAP -> api.getMidCapIndex()
                else -> api.getExternalIndex()
            }
            database.saveIndexes(indexes, type)
            indexes
        }
    }

    override suspend fun getCurrencyPairs(baseCurrency: FxBaseCurrency): List<FxModel> {
        return withContext(IO) {
            val currencies = when (baseCurrency) {
                FxBaseCurrency.EUR -> api.getCurrencyPairs(eur = true)
                FxBaseCurrency.USD -> api.getCurrencyPairs(usd = true)
                FxBaseCurrency.CHF -> api.getCurrencyPairs(chf = true)
            }
            database.saveFx(currencies, FxType.CURRENCY, baseCurrency)
            currencies
        }
    }

    override suspend fun getCurrencyPair(id: Int): FxModel {
        return withContext(IO) {
            val model = api.getCurrencyPair(id)
            database.saveFxDetail(model, FxType.CURRENCY)
            model
        }
    }

    override suspend fun getPreciousMetals(baseCurrency: FxBaseCurrency): List<FxModel> {
        return withContext(IO) {
            val currencies = when (baseCurrency) {
                FxBaseCurrency.EUR -> api.getPreciousMetals(eur = true)
                FxBaseCurrency.USD -> api.getPreciousMetals(usd = true)
                FxBaseCurrency.CHF -> api.getPreciousMetals(chf = true)
            }
            database.saveFx(currencies, FxType.METAL, baseCurrency)
            currencies
        }
    }

    override suspend fun getPreciousMetal(id: Int): FxModel {
        return withContext(IO) {
            val model = api.getPreciousMetal(id)
            database.saveFxDetail(model, FxType.METAL)
            model
        }
    }

    override suspend fun getUnderlyings(type: UnderlyingType?): List<UnderlyingModel> {
        return withContext(IO) {
            val items = when (type) {
                UnderlyingType.SMI -> api.getSMIUnderlyings()
                UnderlyingType.MIDCAP -> api.getMidCapUnderlyings()
                else -> api.getAllUnderlyings()
            }
            type?.let { database.saveUnderlyings(items, type) }
            items
        }
    }

    override suspend fun getUnderlying(id: Int): UnderlyingModel {
        return withContext(IO) {
            val item = api.getUnderlying(id)
            database.saveUnderlying(item)
            item
        }
    }

    override suspend fun getUserWarrantsFilter(productId: Int?): FilterModel? {
        return withContext(IO) {
            val filter = api.getUserWarransFilter(productId)
            if (filter.contractOption == null
                    && filter.maturityEndDate == null
                    && filter.maturityStartDate == null
                    && filter.strikePriceMax == null
                    && filter.strikePriceMin == null
                    && filter.tradedVolumeMax == null
                    && filter.tradedVolume == null) null else filter
        }
    }
}