package com.juliusbaer.premarket.app

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import com.juliusbaer.premarket.core.di.AppInitializer
import javax.inject.Inject

class DateInitializer @Inject constructor(): AppInitializer {
    override fun init(application: Application) {
        AndroidThreeTen.init(application)
    }
}