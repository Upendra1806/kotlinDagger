package com.juliusbaer.premarket

import android.app.Application
import android.os.Handler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.juliusbaer.premarket.core.di.AppInitializer
import com.juliusbaer.premarket.di.RealAppComponent
import com.juliusbaer.premarket.di.RealAppProvider
import dagger.android.AndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class TestApplication : Application(),
        HasAndroidInjector,
        RealAppProvider,
        LifecycleObserver {

    @Inject
    lateinit var initializers: Set<@JvmSuppressWildcards AppInitializer>

    private lateinit var appInjector: AndroidInjector<Any>

    override var wasInBackground = false
        private set

    override var isInBackground = true
        private set

    override val component: RealAppComponent
        get() = appInjector as RealAppComponent

    override fun onCreate() {
        super.onCreate()

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    fun injectIfNeeded(injector: AndroidInjector<Any>) {
        if (!::appInjector.isInitialized) {
            appInjector = injector
            injector.inject(this)

            initializers.forEach {
                it.init(this)
            }
        }
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

    override fun androidInjector(): AndroidInjector<Any> {
        return appInjector
    }
}