package com.juliusbaer.premarket.ui.markets.fx

import androidx.lifecycle.ViewModel
import com.juliusbaer.premarket.core.di.ChildChildFragmentScoped
import com.juliusbaer.premarket.core.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
internal abstract class FxModule {
    @ChildChildFragmentScoped
    @ContributesAndroidInjector
    internal abstract fun fxFragment(): FxFragment

    @Binds
    @IntoMap
    @ViewModelKey(FxViewModel::class)
    abstract fun bindFxViewModel(fxViewModel: FxViewModel): ViewModel
}