package com.juliusbaer.premarket.ui.main

import androidx.lifecycle.ViewModel
import com.juliusbaer.premarket.core.di.FragmentScoped
import com.juliusbaer.premarket.core.di.ViewModelKey
import com.juliusbaer.premarket.ui.alerts.AlertsModule
import com.juliusbaer.premarket.ui.company.CompanyModule
import com.juliusbaer.premarket.ui.company.topWarrants.CompanyTopWarrantsModule
import com.juliusbaer.premarket.ui.contact.ContactModule
import com.juliusbaer.premarket.ui.detailNews.NewsDetailModule
import com.juliusbaer.premarket.ui.detailWarrant.WarrantDetailModule
import com.juliusbaer.premarket.ui.indexDetail.IndexDetailModule
import com.juliusbaer.premarket.ui.markets.MarketsModule
import com.juliusbaer.premarket.ui.markets.fx.FxDetailModule
import com.juliusbaer.premarket.ui.mostActive.MostActiveModule
import com.juliusbaer.premarket.ui.news.NewsModule
import com.juliusbaer.premarket.ui.settings.SettingsModule
import com.juliusbaer.premarket.ui.underluings.UnderlyingsModule
import com.juliusbaer.premarket.ui.warrants.WarrantsModule
import com.juliusbaer.premarket.ui.watchlist.WatchlistModule
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
internal abstract class HomeModule {
    @FragmentScoped
    @ContributesAndroidInjector(modules = [
        WatchlistModule::class,
        NewsModule::class,
        WarrantsModule::class,
        SettingsModule::class,
        MarketsModule::class,
        MostActiveModule::class,
        ContactModule::class,
        AlertsModule::class,
        WarrantDetailModule::class,
        UnderlyingsModule::class,
        IndexDetailModule::class,
        CompanyModule::class,
        FxDetailModule::class,
        NewsDetailModule::class,
        CompanyTopWarrantsModule::class
    ])
    internal abstract fun homeFragment(): HomeFragment

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    abstract fun bindHomeViewModel(homeViewModel: HomeViewModel): ViewModel
}