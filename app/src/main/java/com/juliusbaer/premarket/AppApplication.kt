package com.juliusbaer.premarket

import android.os.Handler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.juliusbaer.premarket.core.BuildConfig
import com.juliusbaer.premarket.core.di.AppInitializer
import com.juliusbaer.premarket.dataFlow.IUserStorage
import com.juliusbaer.premarket.di.AppComponent
import com.juliusbaer.premarket.di.DaggerAppComponent
import com.juliusbaer.premarket.di.RealAppComponent
import com.juliusbaer.premarket.di.RealAppProvider
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import net.hockeyapp.android.CrashManager
import net.hockeyapp.android.CrashManagerListener
import javax.inject.Inject


class AppApplication : DaggerApplication(), RealAppProvider, LifecycleObserver {

    @Inject
    lateinit var initializers: Set<@JvmSuppressWildcards AppInitializer>

    private lateinit var appInjector: AppComponent

    @Inject lateinit var storage: IUserStorage

    override var wasInBackground = false
        private set

    override var isInBackground = true
        private set

    override val component: RealAppComponent
        get() = appInjector

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            if ("dev" != BuildConfig.FLAVOR) {
                CrashManager.register(this, MyCustomCrashManagerListener())
            }
        }
        initializers.forEach {
            it.init(this)
        }
        storage.noInternetAlertShown = false

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun applicationInjector(): AndroidInjector<out AppApplication> {
        val component = DaggerAppComponent
                .builder()
                .create(this)
                .build()
        appInjector = component
        return component
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        isInBackground = true
        wasInBackground = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        isInBackground = false
        Handler().post {
            wasInBackground = false
        }
    }

    private class MyCustomCrashManagerListener: CrashManagerListener() {
        override fun shouldAutoUploadCrashes(): Boolean {
            return true
        }
    }
}