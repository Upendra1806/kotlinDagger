package com.juliusbaer.premarket.ui.filter

import androidx.lifecycle.ViewModel
import com.juliusbaer.premarket.core.di.FragmentScoped
import com.juliusbaer.premarket.core.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
internal abstract class FilterModule {
    @FragmentScoped
    @ContributesAndroidInjector
    internal abstract fun filterFragment(): FilterFragment

    @Binds
    @IntoMap
    @ViewModelKey(FilterViewModel::class)
    abstract fun bindFilterViewModel(filterViewModel: FilterViewModel): ViewModel
}