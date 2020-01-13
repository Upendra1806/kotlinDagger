package com.juliusbaer.premarket.jobs

import android.content.Context
import androidx.work.*
import com.juliusbaer.premarket.di.RealAppComponentProvider
import com.juliusbaer.premarket.stat.StatisticsManager
import timber.log.Timber
import javax.inject.Inject

class SendStatisticsJob(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    @Inject
    lateinit var statisticsManager: StatisticsManager

    init {
        (context.applicationContext as RealAppComponentProvider).component.inject(this)
    }

    override suspend fun doWork(): Result {
        return try {
            statisticsManager.sendStatistics()

            Result.success()
        } catch (throwable: Throwable) {
            Timber.e(throwable)
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "SendStatistics"

        fun scheduleJob(context: Context) {
            val infos = WorkManager.getInstance(context).getWorkInfosByTag(TAG).get()
            if (infos.size > 0 && (infos[0].state == WorkInfo.State.ENQUEUED || infos[0].state == WorkInfo.State.RUNNING)) {
                return
            }
            WorkManager.getInstance(context)
                    .enqueue(OneTimeWorkRequestBuilder<SendStatisticsJob>()
                            .addTag(TAG)
                            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                            .build())
        }
    }
}