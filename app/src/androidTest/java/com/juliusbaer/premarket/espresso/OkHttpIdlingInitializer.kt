package com.juliusbaer.premarket.espresso

import android.app.Application
import androidx.test.espresso.IdlingRegistry
import com.jakewharton.espresso.OkHttp3IdlingResource
import com.juliusbaer.premarket.core.di.AppInitializer
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Named

class OkHttpIdlingInitializer @Inject constructor(
        @Named("okhttpAuth") private val okhttpAuth: OkHttpClient,
        @Named("okhttp") private val okhttp: OkHttpClient): AppInitializer {
    override fun init(application: Application) {
        IdlingRegistry.getInstance().apply {
            register(OkHttp3IdlingResource.create("OkHttpAuth", okhttpAuth))
            register(OkHttp3IdlingResource.create("OkHttp", okhttp))
        }
    }
}