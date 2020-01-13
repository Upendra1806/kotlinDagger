package com.juliusbaer.premarket.dataFlow.network

import com.juliusbaer.premarket.dataFlow.database.model.stat.StatsCollection
import com.juliusbaer.premarket.models.ChartData
import com.juliusbaer.premarket.models.DetailWarrModel
import com.juliusbaer.premarket.models.FilterModel
import com.juliusbaer.premarket.models.serverModels.*
import kotlinx.coroutines.Deferred
import retrofit2.http.*

interface Api {
    @GET("api/CandlesticksGraph/Smi")
    suspend fun getCandleSMI(): List<CandleStickModel>

    @GET("api/CandlesticksGraph/MidCap")
    suspend fun getCandleMidCap(): List<CandleStickModel>

    @GET("api/ManageAlerts")
    suspend fun getAlerts(): List<AlertsModel>

    @POST("api/Alerts/{productId}")
    suspend fun sendAlerts(@Path("productId") productId: Int, @Body alertSendModel: List<AlertSendModel>)

    @DELETE("api/Alerts/{productId}")
    suspend fun deleteAlerts(@Path("productId") productId: Int)

    @GET("api/Underlyings/SMI")
    suspend fun getSMIUnderlyings(): List<UnderlyingModel>

    @GET("api/Underlyings")
    suspend fun getAllUnderlyings(): List<UnderlyingModel>

    @GET("api/Underlyings/MidCap")
    suspend fun getMidCapUnderlyings(): List<UnderlyingModel>

    @GET("api/News")
    suspend fun getNews(): List<NewsModel>

    @GET("api/News/{id}")
    suspend fun getNewsById(@Path("id") id: Int): NewsModel

    @GET("api/News")
    suspend fun getNewsByUnderlyingId(@Query("filter.productIds") id: List<Int>): List<NewsModel>

    @FormUrlEncoded
    @POST("token")
    suspend fun registration(@Field("email") email: String?, @Field("client_id") installationId: String, @Field("grant_type") grant_type: String, @Field("device_token") device_token: String?): RegistrationModel

    @GET("api/Underlying/{id}")
    suspend fun getUnderlying(@Path("id") id: Int?): UnderlyingModel

    @GET("api/UserProfile")
    suspend fun getUserProfile(): UserInfoModel

    @PUT("api/UserProfile")
    suspend fun putUserProfile(@Body userInfoModel: UserInfoModel)

    @GET("api/Warrants")
    suspend fun getWarrants(
            @Query("warrantsView") warrantsView: Boolean?,
            @Query("filter.productId") id: Int?,
            @Query("filter.contractOption") contractOption: String?,
            @Query("filter.maturityStartDate") maturityStartDate: Long?,
            @Query("filter.maturityEndDate") maturityEndDate: Long?,
            @Query("filter.strikePriceMin") strikePriceMin: Double?,
            @Query("filter.strikePriceMax") strikePriceMax: Double?,
            @Query("filter.topOnly") topOnly: Boolean?,
            @Query("filter.categories[]") categories: List<Int>?,
            @Query("filter.tradedVolumeMin") tradedVolume: Int?,
            @Query("filter.pageNumber") pageNumber: Int?,
            @Query("filter.pageSize") pageSize: Int?): List<WarrantModel>

    @GET("api/ExtremeWarrantsFilterValues")
    suspend fun getExtremeWarrantValues(@Query("productId") productId: Int? = null): ExtremeWarrantModel

    @GET("api/Warrant/{id}")
    suspend fun getWarrantById(@Path("id") id: Int): DetailWarrModel

    @GET("api/Indices/SMI")
    suspend fun getSmiIndex(): List<IndexModel>

    @GET("api/Indices/MidCap")
    suspend fun getMidCapIndex(): List<IndexModel>

    @GET("api/Indices/external")
    suspend fun getExternalIndex(): List<IndexModel>

    @POST("api/WatchListItem")
    suspend fun addWatchListItem(@Query("id") id: Int)

    @GET("api/WatchListInfo")
    suspend fun getWatchList(): WatchlistInfoModel

    @DELETE("api/WatchListItem")
    suspend fun deleteWatchListItem(@Query("id") id: Int)

    @GET("api/PerformanceSeries/{id}")
    suspend fun getChartDataResult(@Path("id") id: Int, @Query("period") period: String): ChartData

    @GET("api/Index/{id}")
    suspend fun getIndex(@Path("id") id: Int): IndexModel

    @GET("api/Alerts/{productId}")
    suspend fun getAlert(@Path("productId") id: Int): List<AlertItemModel>

    @GET("api/Logout")
    suspend fun logout()

    @GET("api/Promotion")
    fun getPromotionInfo(): Deferred<PromotionInfoModel?>

    @DELETE("api/Alerts")
    suspend fun alertDeleteAll()

    @DELETE("api/News/{id}")
    suspend fun newsDel(@Path("id") id: Int)

    @GET("api/Warrants/MostActive")
    suspend fun getMostActive(): List<WarrantModel>

    @POST("api/Statistics/Save")
    suspend fun saveStatistics(@Body statsCollection: StatsCollection)

    @GET("api/UserWarrantsFilter")
    suspend fun getUserWarransFilter(@Query("productId") productId: Int? = null): FilterModel

    @GET("api/CurrencyPairs")
    suspend fun getCurrencyPairs(@Query("filter.selectEUR") eur: Boolean = false,
                                 @Query("filter.selectUSD") usd: Boolean = false,
                                 @Query("filter.selectCHF") chf: Boolean = false,
                                 @Query("filter.pageNumber") pageNumber: Int? = null,
                                 @Query("filter.pageSize") pageSize: Int? = null): List<FxModel>

    @GET("api/CurrencyPair/{id}")
    suspend fun getCurrencyPair(@Path("id") id: Int): FxModel

    @GET("api/PreciousMetals")
    suspend fun getPreciousMetals(@Query("filter.selectEUR") eur: Boolean = false,
                                 @Query("filter.selectUSD") usd: Boolean = false,
                                 @Query("filter.selectCHF") chf: Boolean = false,
                                 @Query("filter.pageNumber") pageNumber: Int? = null,
                                 @Query("filter.pageSize") pageSize: Int? = null): List<FxModel>

    @GET("api/PreciousMetal/{id}")
    suspend fun getPreciousMetal(@Path("id") id: Int): FxModel
}