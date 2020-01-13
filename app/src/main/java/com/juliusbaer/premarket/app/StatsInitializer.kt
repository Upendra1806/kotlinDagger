package com.juliusbaer.premarket.app

import android.app.Application
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.juliusbaer.premarket.core.di.AppInitializer
import com.juliusbaer.premarket.jobs.SendStatisticsJob
import com.juliusbaer.premarket.stat.StatTimer
import com.juliusbaer.premarket.stat.StatisticsManager
import com.juliusbaer.premarket.core.utils.CoroutinesDispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class StatsInitializer @Inject constructor(
        private val context: Context,
        private val statisticsManager: StatisticsManager,
        private val statisticsTimer: StatTimer
): AppInitializer, LifecycleObserver {
    override fun init(application: Application) {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        statisticsTimer.stop()

        Timber.d("Application was moved to background")
        GlobalScope.launch(IO) {
            try {
                statisticsManager.onSessionEnd()
            } catch (throwable: Throwable) {
                Timber.e(throwable)
            }
            SendStatisticsJob.scheduleJob(context)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        Timber.d("Application was moved to foreground")
        GlobalScope.launch(IO) {
            try {
                statisticsManager.onSessionStart()
            } catch (throwable: Throwable) {
                Timber.e(throwable)
            }
        }
        statisticsTimer.start()
    }
}