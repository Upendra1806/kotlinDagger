package com.juliusbaer.premarket.ui.indexDetail.actualIndex

import androidx.lifecycle.ViewModel
import com.juliusbaer.premarket.core.di.ChildChildFragmentScoped
import com.juliusbaer.premarket.core.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
internal abstract class IndexPerformanceModule {
    @ChildChildFragmentScoped
    @ContributesAndroidInjector
    internal abstract fun indexPerformanceFragment(): IndexPerformanceFragment

    @Binds
    @IntoMap
    @ViewModelKey(IndexPerformanceViewModel::class)
    abstract fun bindActualIndexViewModel(actualIndexViewModel: IndexPerformanceViewModel): ViewModel
}