package com.juliusbaer.premarket.core.di

import android.app.Application

interface AppInitializer {
    fun init(application: Application)
}