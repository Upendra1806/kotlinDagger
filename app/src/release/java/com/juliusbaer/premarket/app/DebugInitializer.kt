package com.juliusbaer.premarket.app

import android.app.Application
import com.juliusbaer.premarket.core.di.AppInitializer
import javax.inject.Inject

class DebugInitializer @Inject constructor(): AppInitializer {
    override fun init(application: Application) {
    }
}
