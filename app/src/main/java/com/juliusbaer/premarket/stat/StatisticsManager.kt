package com.juliusbaer.premarket.stat

import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.juliusbaer.premarket.core.BuildConfig
import com.juliusbaer.premarket.dataFlow.IDataManager
import com.juliusbaer.premarket.dataFlow.database.IDbHelper
import com.juliusbaer.premarket.dataFlow.database.model.stat.NewsReadingsStat
import com.juliusbaer.premarket.dataFlow.database.model.stat.ProductsStat
import com.juliusbaer.premarket.dataFlow.database.model.stat.SessionsStat
import com.juliusbaer.premarket.dataFlow.database.model.stat.StatsCollection
import com.juliusbaer.premarket.dataFlow.network.Api
import com.juliusbaer.premarket.helpers.HardwareIdProvider
import com.juliusbaer.premarket.core.utils.ConnectionTypeHelper
import com.juliusbaer.premarket.core.utils.CoroutinesDispatchers.IO
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class StatisticsManager @Inject constructor(val application: Context,
                                            private val dbHelper: IDbHelper,
                                            private val bankService: Api,
                                            val dataManager: IDataManager) {
    private var manufacturerName: String? = null
    private var deviceName: String? = null
    private var osVersion: String? = null

    private val loadingQueue: MutableList<String>

    private val languageName: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Resources.getSystem().configuration.locales[0].language
    } else {
        Resources.getSystem().configuration.locale.language
    }

    private var sessionStartTime: Long = 0

    private val regionName: String
        get() {
            return Locale.getDefault().country
        }

    private val deviceType: String = if (UiUtils.isTablet(application)) "AT" else "AP"

    private val connectionTypeName: String = ConnectionTypeHelper.getConnectionTypeHumanReadable(application)

    init {
        loadingQueue = ArrayList()
        fillStaticValues()
    }

    /**
     * Fill static fields of statistics object as regionName, language, deviceName manufacturerName, deviceName model
     */
    private fun fillStaticValues() {
        manufacturerName = Build.MANUFACTURER
        deviceName = Build.MODEL
        osVersion = getOsVersion()
    }

    fun onSessionStart() {
        Timber.d("Session start event triggered")

        sessionStartTime = System.currentTimeMillis() / 1000
    }

    fun onSessionEnd() {
        if (sessionStartTime > 0) {
            Timber.d("Session end event triggered")

            val session = SessionsStat()
            session.connectionType = connectionTypeName
            session.startTime = sessionStartTime
            session.endTime = System.currentTimeMillis() / 1000
            dbHelper.saveSessionStat(session)

            sessionStartTime = 0
        }
    }

    fun onNewsEnd(id: Int, startTimeMillis: Long, sourceTypeId: Int) {
        Timber.d("Session newsEnd event triggered")
        val newsReadingStat = NewsReadingsStat()
        newsReadingStat.newsId = id
        newsReadingStat.readingLengthSec = (System.currentTimeMillis() - startTimeMillis) / 1000
        newsReadingStat.readingTime = startTimeMillis / 1000
        newsReadingStat.sourceTypeId = sourceTypeId
        dbHelper.saveNewsStat(newsReadingStat)
    }

    fun onProductStart(id: Int, eventTypeId: Int, sourceTypeId: Int) {
        val productsStat = ProductsStat()
        productsStat.eventTypeId = eventTypeId
        productsStat.productId = id
        productsStat.sourceTypeId = sourceTypeId
        dbHelper.saveProductStat(productsStat)
    }

    suspend fun sendStatistics() {
        withContext(IO) {
            if (dbHelper.isStatisticsAvailable()) {
                val stat = dbHelper.getStatToSend(createStatsCollections())

                bankService.saveStatistics(stat)
                Timber.d("Statistic was successfully send $stat")

                dbHelper.clearStatisticsByGuid("")
                Timber.d("Statistic was successfully deleted fom db")
            }
        }
    }

    private fun createStatsCollections(): StatsCollection {
        return StatsCollection().apply {
            appVersion = BuildConfig.VERSION_NAME
            deviceType = this@StatisticsManager.deviceType
            device = deviceName
            gUID = HardwareIdProvider(application).deviceId
            language = (languageName)
            manufacturer = (manufacturerName)
            oS = osVersion
            region = regionName
            localTime = Date().time.div(1000)
            pushNotificationsEnabled = NotificationManagerCompat.from(application).areNotificationsEnabled()
        }
    }

    private fun getOsVersion(): String {
        return "Android " + Build.VERSION.RELEASE
    }

}