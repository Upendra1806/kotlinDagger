package com.juliusbaer.premarket.ui.underluings

import androidx.lifecycle.ViewModel
import com.juliusbaer.premarket.core.di.ChildChildFragmentScoped
import com.juliusbaer.premarket.core.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
internal abstract class UnderlyingListModule {
    @ChildChildFragmentScoped
    @ContributesAndroidInjector
    internal abstract fun underlyingListFragment(): UnderlyingListFragment

    @Binds
    @IntoMap
    @ViewModelKey(UnderlyingListViewModel::class)
    abstract fun bindUnderlyingListViewModel(underlyingListViewModel: UnderlyingListViewModel): ViewModel
}