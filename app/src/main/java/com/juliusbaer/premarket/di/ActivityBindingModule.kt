package com.juliusbaer.premarket.di

import com.juliusbaer.premarket.core.di.ActivityScoped
import com.juliusbaer.premarket.ui.alerts.AlertDelActivity
import com.juliusbaer.premarket.ui.alerts.AlertDelModule
import com.juliusbaer.premarket.ui.alerts.UnderlyingAlertsActivity
import com.juliusbaer.premarket.ui.alerts.UnderlyingAlertsModule
import com.juliusbaer.premarket.ui.chart.ChartActivity
import com.juliusbaer.premarket.ui.chart.ChartModule
import com.juliusbaer.premarket.ui.detailWarrant.WarrantDetailModule
import com.juliusbaer.premarket.ui.disclaimer.DisclaimerActivity
import com.juliusbaer.premarket.ui.disclaimer.DisclaimerModule
import com.juliusbaer.premarket.ui.filter.FilterActivity
import com.juliusbaer.premarket.ui.filter.FilterModule
import com.juliusbaer.premarket.ui.login.LoginActivity
import com.juliusbaer.premarket.ui.login.LoginModule
import com.juliusbaer.premarket.ui.main.HomeModule
import com.juliusbaer.premarket.ui.main.MainActivity
import com.juliusbaer.premarket.ui.promotion.PromotionActivity
import com.juliusbaer.premarket.ui.splash.SplashActivity
import com.juliusbaer.premarket.ui.splash.SplashModule
import com.juliusbaer.premarket.ui.warrants.WarrantsModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class ActivityBindingModule {
    @ActivityScoped
    @ContributesAndroidInjector(modules = [HomeModule::class])
    internal abstract fun mainActivity(): MainActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [ChartModule::class])
    internal abstract fun chartActivity(): ChartActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [SplashModule::class])
    internal abstract fun splashActivity(): SplashActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [LoginModule::class])
    internal abstract fun loginActivity(): LoginActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [AlertDelModule::class])
    internal abstract fun alertDelActivity(): AlertDelActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [UnderlyingAlertsModule::class])
    internal abstract fun alertUnderlyingActivity(): UnderlyingAlertsActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules=[FilterModule::class, WarrantsModule::class, WarrantDetailModule::class])
    internal abstract fun filterActivity(): FilterActivity

    @ActivityScoped
    @ContributesAndroidInjector
    internal abstract fun promotionActivity(): PromotionActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules=[DisclaimerModule::class])
    internal abstract fun disclaimerActivity(): DisclaimerActivity
}