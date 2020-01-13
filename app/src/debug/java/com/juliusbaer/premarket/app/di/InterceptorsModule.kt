package com.juliusbaer.premarket.app.di

import com.facebook.stetho.okhttp3.StethoInterceptor
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import okhttp3.Interceptor

@Module
object InterceptorsModule {
    @Provides
    @IntoSet
    @JvmStatic
    fun providesStethoInterceptor(): Interceptor = StethoInterceptor()
}