package com.juliusbaer.premarket.dataFlow

import com.juliusbaer.premarket.helpers.chart.ChartInterval
import com.juliusbaer.premarket.models.ChartData
import com.juliusbaer.premarket.models.DetailWarrModel
import com.juliusbaer.premarket.models.FilterModel
import com.juliusbaer.premarket.models.serverModels.*
import com.juliusbaer.premarket.core.viewmodel.SingleLiveEvent

interface IDataManager {
    val newsLiveData: SingleLiveEvent<List<Int>?>

    suspend fun getMostActiveWarrants(): List<WarrantModel>

    suspend fun getCandles(type: UnderlyingType): List<CandleStickModel>

    suspend fun getUnderlyings(type: UnderlyingType? = null): List<UnderlyingModel>

    suspend fun getWarrants(filterModel: FilterModel?, id: Int?, pageNumber: Int, pageSize: Int): List<WarrantModel>

    suspend fun getExtremeWarrantValues(productId: Int? = null): ExtremeWarrantModel

    suspend fun getUnderlying(id: Int): UnderlyingModel

    suspend fun getIndexes(type: UnderlyingType? = null): List<IndexModel>

    suspend fun getWarrant(id: Int): DetailWarrModel

    suspend fun addWatchListItem(id: Int)

    suspend fun deleteWatchListItem(id: Int)

    suspend fun getWatchList(): WatchlistInfoModel

    suspend fun getAlerts(): List<AlertsModel>

    suspend fun deleteAlert(productId: Int)

    suspend fun setAlert(productId: Int, alertSendModel: List<AlertSendModel>)

    suspend fun getUserInfo(): UserInfoModel

    suspend fun putUserInfo(userInfoModel: UserInfoModel)

    suspend fun getNews(): List<NewsModel>

    suspend fun getNewsById(id: Int): NewsModel

    suspend fun getNewsByUnderlyingId(id: Int): List<NewsModel>

    suspend fun getChartDataResult(id: Int, period: ChartInterval): ChartData

    suspend fun getIndex(id: Int): IndexModel

    suspend fun getAlert(id: Int): List<AlertItemModel>

    suspend fun logout()

    suspend fun getPromotionInfo(): PromotionInfoModel?

    suspend fun alertDeleteAll()
    suspend fun newsDel(id: Int)
    suspend fun getUserWarrantsFilter(productId: Int?): FilterModel?
    suspend fun getCurrencyPairs(baseCurrency: FxBaseCurrency): List<FxModel>
    suspend fun getCurrencyPair(id: Int): FxModel
    suspend fun getPreciousMetals(baseCurrency: FxBaseCurrency): List<FxModel>
    suspend fun getPreciousMetal(id: Int): FxModel
}