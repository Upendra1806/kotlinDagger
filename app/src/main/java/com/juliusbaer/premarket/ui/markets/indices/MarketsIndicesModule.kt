package com.juliusbaer.premarket.ui.markets.indices

import androidx.lifecycle.ViewModel
import com.juliusbaer.premarket.core.di.ChildChildFragmentScoped
import com.juliusbaer.premarket.core.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
internal abstract class MarketsIndicesModule {
    @ChildChildFragmentScoped
    @ContributesAndroidInjector
    internal abstract fun marketsIndicesFragment(): MarketsIndicesFragment

    @Binds
    @IntoMap
    @ViewModelKey(MarketsIndicesViewModel::class)
    abstract fun bindMarketsIndicesViewModel(marketsIndicesViewModel: MarketsIndicesViewModel): ViewModel
}