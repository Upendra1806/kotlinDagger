package com.juliusbaer.premarket.dataFlow.database

import android.util.SparseArray
import com.juliusbaer.premarket.dataFlow.database.model.stat.NewsReadingsStat
import com.juliusbaer.premarket.dataFlow.database.model.stat.ProductsStat
import com.juliusbaer.premarket.dataFlow.database.model.stat.SessionsStat
import com.juliusbaer.premarket.dataFlow.database.model.stat.StatsCollection
import com.juliusbaer.premarket.helpers.chart.ChartInterval
import com.juliusbaer.premarket.models.*
import com.juliusbaer.premarket.models.serverModels.*
import com.juliusbaer.premarket.core.utils.CoroutinesDispatchers.IO
import io.realm.Realm
import io.realm.RealmList
import io.realm.kotlin.where
import kotlinx.coroutines.withContext

class DbHelper : IDbHelper {
    override fun saveFx(items: List<FxModel>, type: FxType, baseCurrency: FxBaseCurrency) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction { r ->
                var chf = false
                var usd = false
                var eur = false

                when (baseCurrency) {
                    FxBaseCurrency.CHF -> chf = true
                    FxBaseCurrency.USD -> usd = true
                    FxBaseCurrency.EUR -> eur = true
                }
                r.where(FxModel::class.java)
                        .equalTo(FxModelFields.TYPE, type.ordinal)
                        .equalTo(FxModelFields.CHF, chf)
                        .equalTo(FxModelFields.USD, usd)
                        .equalTo(FxModelFields.EUR, eur)
                        .findAll()
                        .deleteAllFromRealm()

                if (items.isNotEmpty()) {
                    val map = SparseArray<FxModel>(items.size)
                    for (item in items) {
                        map.put(item.id, item)

                        item.chf = chf
                        item.usd = usd
                        item.eur = eur
                        item.type = type.ordinal
                    }
                    r.where(FxModel::class.java)
                            .`in`(FxModelFields.ID, items.map { it.id }.toTypedArray())
                            .findAll()
                            .forEach {
                                val model = map[it.id]
                                model.chf = chf or it.chf
                                model.usd = usd or it.usd
                                model.eur = eur or it.eur
                            }
                    r.insertOrUpdate(items)
                }
            }
        }
    }

    override suspend fun getFx(type: FxType, baseCurrency: FxBaseCurrency): List<FxModel> {
        return withContext(IO) {
            Realm.getDefaultInstance().use {
                val q = it.where(FxModel::class.java)
                        .equalTo(FxModelFields.TYPE, type.ordinal)
                when (baseCurrency) {
                    FxBaseCurrency.CHF -> q.equalTo(FxModelFields.CHF, true)
                    FxBaseCurrency.USD -> q.equalTo(FxModelFields.USD, true)
                    FxBaseCurrency.EUR -> q.equalTo(FxModelFields.EUR, true)
                }
                it.copyFromRealm(q.findAll())
            }
        }
    }

    override fun saveFxDetail(item: FxModel, type: FxType) {
        return Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction { r ->
                r.where(FxModel::class.java)
                        .equalTo(FxModelFields.ID, item.id)
                        .findFirst()?.let {
                            item.precision = it.precision
                            item.type = it.type
                            item.chf = it.chf
                            item.usd = it.usd
                            item.eur = it.eur
                        }
                r.insertOrUpdate(item)
            }
        }
    }

    override suspend fun getFxDetail(id: Int, type: FxType): FxModel? {
        return withContext(IO) {
            Realm.getDefaultInstance().use { r ->
                r.where(FxModel::class.java)
                        .equalTo(FxModelFields.ID, id)
                        .equalTo(FxModelFields.TYPE, type.ordinal)
                        .findFirst()?.let {
                            r.copyFromRealm(it)
                        }
            }
        }

    }

    override fun clearCaches() {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {
                it.delete(FxModel::class.java)
                it.delete(DetailWarrModel::class.java)
                it.delete(ChartModel::class.java)
                it.delete(ChartCoordModel::class.java)
                it.delete(CandleStickModel::class.java)
                it.delete(AlertsModel::class.java)
                it.delete(NewsModel::class.java)
                it.delete(WarrantModel::class.java)
                it.delete(IndexModel::class.java)
                it.delete(UnderlyingModel::class.java)
            }
        }
    }

    override fun saveStatToSend(stat: StatsCollection) {
        Realm.getDefaultInstance().use {
            it.executeTransactionAsync { r ->
                r.copyToRealmOrUpdate(stat)
            }
        }
    }

    override fun saveNewsStat(news: NewsReadingsStat) {
        Realm.getDefaultInstance().use {
            it.executeTransactionAsync { r ->
                r.copyToRealm(news)
            }
        }
    }

    override fun saveProductStat(productsStat: ProductsStat) {
        Realm.getDefaultInstance().use {
            it.executeTransactionAsync { r ->
                r.copyToRealm(productsStat)
            }
        }
    }

    override fun clearStatisticsByGuid(guid: String) {
        Realm.getDefaultInstance().use {
            it.executeTransaction { r ->
                r.delete(NewsReadingsStat::class.java)
                r.delete(SessionsStat::class.java)
                r.delete(ProductsStat::class.java)
            }
        }
    }

    override fun isStatisticsAvailable(): Boolean {
        return Realm.getDefaultInstance().use { realm ->
            getNewsStat(realm).isNotEmpty() || getLastSession(realm).isNotEmpty() || getProduct(realm).isNotEmpty()
        }
    }

    override fun saveSessionStat(sessionsStatItem: SessionsStat) {
        Realm.getDefaultInstance().use {
            it.executeTransaction { r ->
                r.insert(sessionsStatItem)
            }
        }
    }

    override fun getStatToSend(stat: StatsCollection): StatsCollection {
        Realm.getDefaultInstance().use { realm ->
            stat.sessionsStat = realm.copyFromRealm(getLastSession(realm))
            stat.newsReadingsStat = realm.copyFromRealm(getNewsStat(realm))
            stat.productsStat = realm.copyFromRealm(getProduct(realm))
        }
        return stat
    }

    private fun getProduct(realm: Realm): List<ProductsStat> {
        return realm.where(ProductsStat::class.java).findAll()
    }

    private fun getNewsStat(realm: Realm): List<NewsReadingsStat> {
        return realm.where(NewsReadingsStat::class.java).findAll()
    }

    private fun getLastSession(realm: Realm): List<SessionsStat> {
        return realm.where(SessionsStat::class.java).findAll()
    }

    override fun saveAlerts(items: List<AlertsModel>) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {
                it.delete(AlertsModel::class.java)
                it.insert(items)
            }
        }
    }

    override suspend fun getAlerts(): List<AlertsModel> {
        return withContext(IO) {
            Realm.getDefaultInstance().use {
                it.copyFromRealm(it.where(AlertsModel::class.java)
                        .findAll())
            }
        }
    }

    override fun saveUnderlyings(items: List<UnderlyingModel>, type: UnderlyingType) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {r ->
                var isSmi = false
                var isMidCap = false

                when (type) {
                    UnderlyingType.SMI -> isSmi = true
                    UnderlyingType.MIDCAP -> isMidCap = true
                }
                r.where(UnderlyingModel::class.java)
                        .equalTo(UnderlyingModelFields.IS_SMI, isSmi)
                        .equalTo(UnderlyingModelFields.IS_MID_CAP, isMidCap)
                        .findAll()
                        .deleteAllFromRealm()

                if (items.isNotEmpty()) {
                    val map = SparseArray<UnderlyingModel>(items.size)
                    for (item in items) {
                        item.isSmi = isSmi
                        item.isMidCap = isMidCap

                        map.put(item.id, item)
                    }
                    r.where(UnderlyingModel::class.java)
                            .`in`(UnderlyingModelFields.ID, items.map { it.id }.toTypedArray())
                            .findAll()
                            .forEach {
                                val model = map[it.id]
                                model.isSmi = isSmi or it.isSmi
                                model.isMidCap = isMidCap or it.isMidCap
                                //preserve item's detail fields
                                model.isInWatchList = it.isInWatchList
                            }
                    r.insertOrUpdate(items)
                }
            }
        }
    }

    override suspend fun getUnderlyings(type: UnderlyingType): List<UnderlyingModel> {
        return withContext(IO) {
            Realm.getDefaultInstance().use {
                val q = it.where(UnderlyingModel::class.java)
                when (type) {
                    UnderlyingType.SMI -> q.equalTo(UnderlyingModelFields.IS_SMI, true)
                    UnderlyingType.MIDCAP -> q.equalTo(UnderlyingModelFields.IS_MID_CAP, true)
                }
                it.copyFromRealm(q.findAll())
            }
        }
    }

    override fun saveIndexes(items: List<IndexModel>, type: UnderlyingType?) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction { r ->
                var isSmi = false
                var isMidCap = false
                var isMarkets = false

                when (type) {
                    UnderlyingType.SMI -> isSmi = true
                    UnderlyingType.MIDCAP -> isMidCap = true
                    else -> isMarkets = true
                }
                r.where(IndexModel::class.java)
                        .equalTo(IndexModelFields.IS_SMI, isSmi)
                        .equalTo(IndexModelFields.IS_MID_CAP, isMidCap)
                        .equalTo(IndexModelFields.IS_MARKETS, isMarkets)
                        .findAll()
                        .deleteAllFromRealm()

                if (items.isNotEmpty()) {
                    val map = SparseArray<IndexModel>(items.size)
                    for (item in items) {
                        item.isSmi = isSmi
                        item.isMidCap = isMidCap
                        item.isMarkets = isMarkets

                        map.put(item.id, item)
                    }
                    r.where(IndexModel::class.java)
                            .`in`(IndexModelFields.ID, items.map { it.id }.toTypedArray())
                            .findAll()
                            .forEach {
                                val model = map[it.id]
                                model.isSmi = isSmi or it.isSmi
                                model.isMidCap = isMidCap or it.isMidCap
                                model.isMarkets = isMarkets or it.isMarkets
                            }
                    r.insertOrUpdate(items)
                }
            }
        }
    }

    override suspend fun getIndexes(type: UnderlyingType?): List<IndexModel> {
        return withContext(IO) {
            Realm.getDefaultInstance().use {
                val q = it.where(IndexModel::class.java)
                when (type) {
                    UnderlyingType.SMI -> q.equalTo(IndexModelFields.IS_SMI, true)
                    UnderlyingType.MIDCAP -> q.equalTo(IndexModelFields.IS_MID_CAP, true)
                    else -> q.equalTo(IndexModelFields.IS_MARKETS, true)
                }
                it.copyFromRealm(q.findAll())
            }
        }

    }

    override fun saveIndexDetail(index: IndexModel) {
        return Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction { r ->
                r.where(IndexModel::class.java)
                        .equalTo(IndexModelFields.ID, index.id)
                        .findFirst()?.let {
                            index.isSmi = it.isSmi
                            index.isMidCap = it.isMidCap
                            index.isMarkets = it.isMarkets
                        }
                r.insertOrUpdate(index)
            }
        }
    }

    override suspend fun getIndex(id: Int): IndexModel? {
        return withContext(IO) {
            Realm.getDefaultInstance().use {
                val item = it.where(IndexModel::class.java)
                        .equalTo(IndexModelFields.ID, id)
                        .findFirst()
                if (item != null) it.copyFromRealm(item) else null
            }
        }
    }

    override fun saveWarrants(items: List<WarrantModel>, collectionId: Int?, isMostActive: Boolean) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction { r ->
                val query = r.where(WarrantModel::class.java)
                        .equalTo(WarrantModelFields.IS_MOST_ACTIVE, isMostActive)
                if (collectionId != null) {
                    query.equalTo(WarrantModelFields.COLLECTION_ID, collectionId)
                } else {
                    query.isNull(WarrantModelFields.COLLECTION_ID)
                }
                val oldItems = query.findAll()
                if (oldItems.isNotEmpty()) {
                    r.where(DetailWarrModel::class.java)
                            .`in`(DetailWarrModelFields.ID, oldItems.map { it.id }.toTypedArray())
                            .findAll()
                            .deleteAllFromRealm()
                    oldItems.deleteAllFromRealm()
                }
                if (items.isNotEmpty()) {
                    r.where(DetailWarrModel::class.java)
                            .`in`(DetailWarrModelFields.ID, items.map { it.id }.toTypedArray())
                            .findAll()
                            .deleteAllFromRealm()
                    items.forEach { item ->
                        item.collectionId = collectionId
                        item.isMostActive = isMostActive
                    }
                    r.insert(items)
                }
            }
        }
    }

    override suspend fun getWarrants(collectionId: Int?, isMostActive: Boolean, isTop: Boolean?): List<WarrantModel> {
        return withContext(IO) {
            Realm.getDefaultInstance().use {
                val query = it.where(WarrantModel::class.java)
                        .equalTo(WarrantModelFields.IS_MOST_ACTIVE, isMostActive)
                if (isTop != null) {
                    query.equalTo(WarrantModelFields.IS_TOP, isTop)
                }
                if (collectionId != null) {
                    query.equalTo(WarrantModelFields.COLLECTION_ID, collectionId)
                } else {
                    query.isNull(WarrantModelFields.COLLECTION_ID)
                }
                it.copyFromRealm(query.findAll())
            }
        }
    }

    override fun saveNews(items: List<NewsModel>, collectionId: Int?) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {
                val query = it.where(NewsModel::class.java)
                if (collectionId != null) {
                    query.equalTo(NewsModelFields.COLLECTION_ID, collectionId)
                } else {
                    query.isNull(NewsModelFields.COLLECTION_ID)
                }
                query.findAll().deleteAllFromRealm()

                items.forEach { item -> item.collectionId = collectionId }
                it.insert(items)
            }
        }
    }

    override suspend fun getUnderlying(id: Int): UnderlyingModel? {
        return withContext(IO) {
            Realm.getDefaultInstance().use { realm ->
                realm.where<UnderlyingModel>()
                        .equalTo(UnderlyingModelFields.ID, id)
                        .findFirst()?.let {
                            realm.copyFromRealm(it)
                        }
            }
        }
    }

    override fun saveUnderlying(item: UnderlyingModel) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction { r ->
                r.where(UnderlyingModel::class.java)
                        .equalTo(UnderlyingModelFields.ID, item.id)
                        .findFirst()?.let {
                            item.isSmi = it.isSmi
                            item.isMidCap = it.isMidCap
                        }
                r.insertOrUpdate(item)
            }
        }
    }

    override suspend fun getWarrantDetail(id: Int): DetailWarrModel? {
        return withContext(IO) {
            Realm.getDefaultInstance().use { realm ->
                realm.where<DetailWarrModel>()
                        .equalTo(DetailWarrModelFields.ID, id)
                        .findFirst()?.let {
                            realm.copyFromRealm(it)
                        }
            }
        }
    }

    override fun saveWarrant(item: DetailWarrModel) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {
                it.insertOrUpdate(item)
            }
        }
    }

    override suspend fun updateCandleStickFromSocketModel(socketModel: CandleStickUpdateModel, type: UnderlyingType): Boolean {
        return withContext(IO) {
            Realm.getDefaultInstance().use { realm ->
                var result = false
                realm.executeTransaction { r ->
                    result = r.where<CandleStickModel>()
                            .equalTo(CandleStickModelFields.PRODUCT_ID, socketModel.id)
                            .equalTo(CandleStickModelFields.TYPE, type.ordinal)
                            .findFirst()?.let {
                                it.updateFromSocketModel(socketModel)
                                true
                            } ?: false
                }
                result
            }
        }
    }

    override suspend fun updateUnderlyingFromSocketModel(socketModel: ProductUpdateModel): Boolean {
        return withContext(IO) {
            Realm.getDefaultInstance().use { realm ->
                var result = false
                realm.executeTransaction { r ->
                    result = r.where<UnderlyingModel>()
                            .equalTo(UnderlyingModelFields.ID, socketModel.id)
                            .findFirst()?.let {
                                it.updateFromSocketModel(socketModel)
                                true
                            } ?: false
                }
                result
            }
        }
    }

    override suspend fun updateWarrantFromSocketModel(socketModel: ProductUpdateModel): Boolean {
        return withContext(IO) {
            Realm.getDefaultInstance().use { realm ->
                var result = false
                realm.executeTransaction { r ->
                    result = r.where<WarrantModel>()
                            .equalTo(WarrantModelFields.ID, socketModel.id)
                            .findFirst()?.let {
                                it.updateFromSocketModel(socketModel)
                                true
                            } ?: false
                }
                result
            }
        }
    }

    override suspend fun updateIndexFromSocketModel(socketModel: ProductUpdateModel): Boolean {
        return withContext(IO) {
            Realm.getDefaultInstance().use { realm ->
                var result = false
                realm.executeTransaction { r ->
                    result = r.where<IndexModel>()
                            .equalTo(IndexModelFields.ID, socketModel.id)
                            .findFirst()?.let {
                                it.updateFromSocketModel(socketModel)
                                true
                            } ?: false
                }
                result
            }
        }
    }


    override suspend fun updateFxItemFromSocketModel(socketModel: ProductUpdateModel): Boolean {
        return withContext(IO) {
            Realm.getDefaultInstance().use { realm ->
                var result = false
                realm.executeTransaction { r ->
                    result = r.where<FxModel>()
                            .equalTo(FxModelFields.ID, socketModel.id)
                            .findFirst()?.let {
                                it.updateFromSocketModel(socketModel)
                                true
                            } ?: false
                }
                result
            }
        }
    }

    override suspend fun getNews(collectionId: Int?): List<NewsModel> {
        return withContext(IO) {
            Realm.getDefaultInstance().use {
                val query = it.where(NewsModel::class.java)
                if (collectionId != null) {
                    query.equalTo(NewsModelFields.COLLECTION_ID, collectionId)
                } else {
                    query.isNull(NewsModelFields.COLLECTION_ID)
                }
                it.copyFromRealm(query.findAll())
            }
        }
    }

    override suspend fun getNewsById(id: Int): NewsModel? {
        return withContext(IO) {
            Realm.getDefaultInstance().use {
                val item = it.where(NewsModel::class.java)
                        .equalTo(NewsModelFields.ID, id)
                        .findFirst()
                if (item != null) it.copyFromRealm(item) else null
            }
        }
    }


    override fun saveCandles(items: List<CandleStickModel>, type: UnderlyingType) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {
                it.where(CandleStickModel::class.java)
                        .equalTo(CandleStickModelFields.TYPE, type.ordinal)
                        .findAll()
                        .deleteAllFromRealm()

                items.forEach { item ->
                    item.type = type.ordinal
                }
                it.insert(items)
            }
        }
    }

    override suspend fun getCandles(type: UnderlyingType): List<CandleStickModel> {
        return withContext(IO) {
            Realm.getDefaultInstance().use {
                it.copyFromRealm(it.where(CandleStickModel::class.java)
                        .equalTo(CandleStickModelFields.TYPE, type.ordinal)
                        .findAll())
            }
        }
    }

    override fun saveChartData(id: Int, period: ChartInterval, chartData: ChartData) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction { r ->
                r.where(ChartModel::class.java)
                        .equalTo(ChartModelFields.ID, id)
                        .equalTo(ChartModelFields.PERIOD, period.v)
                        .findFirst()?.let {
                            it.coords?.deleteAllFromRealm()
                            it.deleteFromRealm()
                        }
                val model = ChartModel().apply {
                    this.id = id
                    this.period = period.v
                    interval = chartData.xAxisInterval

                    coords = RealmList()
                    chartData.data?.forEach { src -> coords!!.add(ChartCoordModel().apply { x = src.x; y = src.y }) }
                }
                r.insert(model)
            }
        }
    }

    override suspend fun getChartData(id: Int, period: String): ChartData? {
        return withContext(IO) {
            Realm.getDefaultInstance().use { realm ->
                realm.where(ChartModel::class.java)
                        .equalTo(ChartModelFields.ID, id)
                        .equalTo(ChartModelFields.PERIOD, period)
                        .findFirst()?.let { model ->
                    ChartData(model.coords?.map { Data(it.x, it.y) }, model.interval)
                }
            }
        }
    }
}