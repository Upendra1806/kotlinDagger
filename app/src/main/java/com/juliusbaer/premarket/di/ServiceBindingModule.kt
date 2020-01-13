package com.juliusbaer.premarket.di

import com.juliusbaer.premarket.core.di.ServiceScoped
import com.juliusbaer.premarket.notification.MyFirebaseInstanceIdService
import com.juliusbaer.premarket.notification.MyFirebaseMessagingService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBindingModule {
    @ServiceScoped
    @ContributesAndroidInjector
    internal abstract fun myFirebaseInstanceIdService(): MyFirebaseInstanceIdService

    @ServiceScoped
    @ContributesAndroidInjector
    internal abstract fun myFirebaseMessagingService(): MyFirebaseMessagingService
}