package com.juliusbaer.premarket.stat

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.juliusbaer.premarket.jobs.SendStatisticsJob
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatTimer @Inject constructor(val context: Context, val statisticsManager: StatisticsManager) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val pendingIntent= PendingIntent.getBroadcast(
            context,
            INTENT_REQUEST_CODE,
            Intent(TIMEOUT_ACTION_NAME),
            PendingIntent.FLAG_UPDATE_CURRENT)

    /**
     * Start timer
     */
    fun start() {
        context.registerReceiver(statisticsTimerEventReceiver, IntentFilter(TIMEOUT_ACTION_NAME))
        alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis() + timeoutMs, timeoutMs, pendingIntent)
        Timber.d("Timer started")
    }

    /**
     * Stop timer
     */
    fun stop() {
        alarmManager.cancel(pendingIntent)
        try {
            context.unregisterReceiver(statisticsTimerEventReceiver)
        } catch (e: Throwable) {
        }
        Timber.d("Timer stopped")
    }

    private val statisticsTimerEventReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            SendStatisticsJob.scheduleJob(context)
        }
    }

    companion object {
        private const val timeoutMs = 10 * 1000L

        private val TIMEOUT_ACTION_NAME = StatTimer::class.java.toString() + ".TIMEOUT"

        private const val INTENT_REQUEST_CODE = 0
    }
}

