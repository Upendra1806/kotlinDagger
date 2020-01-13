package com.juliusbaer.premarket.ui.company.performance

import androidx.lifecycle.ViewModel
import com.juliusbaer.premarket.core.di.ChildChildFragmentScoped
import com.juliusbaer.premarket.core.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
internal abstract class PerformanceModule {
    @ChildChildFragmentScoped
    @ContributesAndroidInjector
    internal abstract fun performanceFragment(): PerformanceFragment

    @Binds
    @IntoMap
    @ViewModelKey(PerformanceViewModel::class)
    abstract fun bindPerformanceViewModel(performanceViewModel: PerformanceViewModel): ViewModel
}