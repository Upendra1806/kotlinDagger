package com.juliusbaer.premarket.ui.chart

import androidx.lifecycle.ViewModel
import com.juliusbaer.premarket.core.di.FragmentScoped
import com.juliusbaer.premarket.core.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
internal abstract class ChartModule {
    @FragmentScoped
    @ContributesAndroidInjector
    internal abstract fun landingChartFragment(): LandingChartFragment

    @Binds
    @IntoMap
    @ViewModelKey(LandingChartViewModel::class)
    abstract fun bindLandingChartViewModel(landingChartViewModel: LandingChartViewModel): ViewModel
}