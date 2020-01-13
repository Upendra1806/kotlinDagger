package com.juliusbaer.premarket.dataFlow.database

import com.juliusbaer.premarket.dataFlow.database.model.stat.NewsReadingsStat
import com.juliusbaer.premarket.dataFlow.database.model.stat.ProductsStat
import com.juliusbaer.premarket.dataFlow.database.model.stat.SessionsStat
import com.juliusbaer.premarket.dataFlow.database.model.stat.StatsCollection
import com.juliusbaer.premarket.helpers.chart.ChartInterval
import com.juliusbaer.premarket.models.ChartData
import com.juliusbaer.premarket.models.DetailWarrModel
import com.juliusbaer.premarket.models.serverModels.*

interface IDbHelper {
    fun saveNewsStat(news: NewsReadingsStat)

    fun saveProductStat(productsStat: ProductsStat)

    fun clearStatisticsByGuid(guid: String)

    fun isStatisticsAvailable(): Boolean

    fun saveSessionStat(sessionsStatItem: SessionsStat)

    fun getStatToSend(stat: StatsCollection): StatsCollection

    fun saveStatToSend(stat: StatsCollection)

    fun saveUnderlyings(items: List<UnderlyingModel>, type: UnderlyingType)
    suspend fun getUnderlyings(type: UnderlyingType): List<UnderlyingModel>
    fun saveIndexes(items: List<IndexModel>, type: UnderlyingType? = null)
    suspend fun getIndexes(type: UnderlyingType? = null): List<IndexModel>
    suspend fun getIndex(id: Int): IndexModel?
    fun saveIndexDetail(index: IndexModel)
    suspend fun updateIndexFromSocketModel(socketModel: ProductUpdateModel): Boolean
    fun saveWarrants(items: List<WarrantModel>, collectionId: Int? = null, isMostActive: Boolean = false)
    suspend fun getWarrants(collectionId: Int? = null, isMostActive: Boolean = false, isTop: Boolean? = null):List<WarrantModel>
    suspend fun updateWarrantFromSocketModel(socketModel: ProductUpdateModel): Boolean
    suspend fun getWarrantDetail(id: Int): DetailWarrModel?
    fun saveWarrant(item: DetailWarrModel)
    fun saveNews(items: List<NewsModel>, collectionId: Int? = null)
    suspend fun getNews(collectionId: Int? = null): List<NewsModel>
    fun clearCaches()
    fun saveAlerts(items: List<AlertsModel>)
    suspend fun getAlerts(): List<AlertsModel>
    fun saveUnderlying(item: UnderlyingModel)
    suspend fun getUnderlying(id: Int): UnderlyingModel?
    fun saveCandles(items: List<CandleStickModel>, type: UnderlyingType)
    suspend fun getCandles(type: UnderlyingType): List<CandleStickModel>
    suspend fun getNewsById(id: Int): NewsModel?
    suspend fun updateUnderlyingFromSocketModel(socketModel: ProductUpdateModel): Boolean
    suspend fun updateCandleStickFromSocketModel(socketModel: CandleStickUpdateModel, type: UnderlyingType): Boolean
    fun saveChartData(id: Int, period: ChartInterval, chartData: ChartData)
    suspend fun getChartData(id: Int, period: String): ChartData?
    fun saveFx(items: List<FxModel>, type: FxType, baseCurrency: FxBaseCurrency)
    suspend fun getFx(type: FxType, baseCurrency: FxBaseCurrency): List<FxModel>
    suspend fun updateFxItemFromSocketModel(socketModel: ProductUpdateModel): Boolean
    suspend fun getFxDetail(id: Int, type: FxType): FxModel?
    fun saveFxDetail(item: FxModel, type: FxType)
}
