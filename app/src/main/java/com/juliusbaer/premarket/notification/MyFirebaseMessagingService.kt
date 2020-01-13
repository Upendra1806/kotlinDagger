package com.juliusbaer.premarket.notification

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.dataFlow.IDataManager
import com.juliusbaer.premarket.dataFlow.IUserStorage
import com.juliusbaer.premarket.models.ProductType
import com.juliusbaer.premarket.ui.main.MainActivity
import com.juliusbaer.premarket.utils.Constants
import com.juliusbaer.premarket.utils.UiUtils
import dagger.android.AndroidInjection
import me.leolin.shortcutbadger.ShortcutBadger
import timber.log.Timber
import javax.inject.Inject

class MyFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        private const val FCM_PARAM = "picture"
        private const val TEXT_KEY = "gcm.notification.body"
        private const val GROUP_KEY = "com.adviscent.jpa.develop.group"

        private val APP_CHANEL = MyFirebaseMessagingService::class.java.canonicalName
    }

    private var NOTIFICATION_ID = 18
    //    val GROUP_KEY = "0|com.adviscent.jpa.develop|0|FCM-Notification:265119115|10303"

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
    }

    private var bundle: Bundle? = null
    @Inject
    lateinit var storage: IUserStorage
    @Inject
    lateinit var dataManager: IDataManager


    override fun handleIntent(intent: Intent) {
        bundle = intent.extras
        //HACK: if message comes with both "data" / "notification" fields, system process notification itself without our app,
        //this hack allows always catch pushes (only for com.google.firebase:firebase-messaging <= 11.4.2)
        //handleIntent is absent in above versions
        if (bundle != null) {
            intent.putExtra("gcm.notification.e", "0")
        }
        super.handleIntent(intent)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        bundle?.let { bundle ->
            Timber.d("Notification keys ${bundle.keySet()}")
            if (isAppForeground()) {
                val productTypeInt = bundle.getString("ProductTypeId")?.toInt() ?: return
                if (productTypeInt == ProductType.NEWS.v) {
                    //supposed list could contains related products ids
                    dataManager.newsLiveData.postValue(emptyList())
                }
            } else {
                updateCounter(bundle)
                showNotification(bundle)
            }
        }
    }

    private fun updateCounter(bundle: Bundle) {
        val ids = bundle.getString(Constants.PUSH_ID_KEY) ?: return
        if (ids.isNotEmpty()) {
            updateCount()
        }
    }

    private fun updateCount() {
        val counter = storage.badgeCount + 1
        ShortcutBadger.applyCount(this, counter)
        storage.badgeCount = counter
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun showNotification(bundle: Bundle?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var text = bundle!!.getString(TEXT_KEY)
        if (TextUtils.isEmpty(text)) {
            text = getString(R.string.notification_text)
        } else if (text!!.indexOf('"') != -1) {
            text = text.substring(text.indexOf('"'), text.length - 1)
        }

        if (UiUtils.isCompatWithO) {
            val notificationChannel = NotificationChannel(APP_CHANEL, getString(R.string.notification_title), NotificationManager.IMPORTANCE_DEFAULT)
            with(notificationChannel) {
                description = bundle.getString("gcm.notification.title")
                setShowBadge(true)
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            }
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val builder = NotificationCompat.Builder(applicationContext, APP_CHANEL)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                .setContentTitle(bundle.getString("gcm.notification.title"))
                .setContentText(text)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setGroup(GROUP_KEY)
                .setGroupSummary(true)


        val intent = Intent(this, MainActivity::class.java).apply {
            action = bundle.get("ProductId").toString()
            putExtras(bundle)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val resultPendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        builder.setContentIntent(resultPendingIntent)

        NOTIFICATION_ID += 1

        notificationManager.notify(bundle.get("ProductId").toString().toInt(), builder.build())
    }


    @RequiresApi(api = 23)
    private fun getPreviousNotifications(newText: String): NotificationCompat.InboxStyle? {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var counter = 0
        val inbox = NotificationCompat.InboxStyle()
        inbox.addLine(newText)
        val previousNotifications = notificationManager.activeNotifications
        previousNotifications.size
        if (previousNotifications?.isNotEmpty()!!) {

            previousNotifications
                    .mapNotNull { it.notification?.extras?.get(NotificationCompat.EXTRA_TEXT_LINES) as? Array<CharSequence> }
                    .filter { it.isNotEmpty() }
                    .forEach {
                        it.forEach { line ->
                            inbox.addLine(line)
                            counter++
                        }
                    }
        }
        inbox.setBigContentTitle(getString(R.string.notification_title))
        if (counter > 0) {
            inbox.setSummaryText(String.format(getString(R.string.new_articles), counter + 1))
        }
        return inbox
    }

    private fun isAppForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return if (activityManager.runningAppProcesses != null) {
            activityManager.runningAppProcesses.any { it.uid == applicationInfo.uid && it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND }
        } else {
            false
        }
    }
}

