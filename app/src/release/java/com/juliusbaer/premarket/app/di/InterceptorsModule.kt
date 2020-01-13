package com.juliusbaer.premarket.app.di

import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet
import okhttp3.Interceptor

@Module
object InterceptorsModule {
    @ElementsIntoSet
    @Provides
    @JvmStatic
    fun providesNetworkInterceptor(): Set<@JvmSuppressWildcards Interceptor> {
        return emptySet()
    }
}