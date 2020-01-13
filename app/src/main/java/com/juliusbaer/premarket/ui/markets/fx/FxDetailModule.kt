package com.juliusbaer.premarket.ui.markets.fx

import androidx.lifecycle.ViewModel
import com.juliusbaer.premarket.core.di.ChildFragmentScoped
import com.juliusbaer.premarket.core.di.ViewModelKey
import com.juliusbaer.premarket.ui.phoneDialog.PhoneDialogModule
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
internal abstract class FxDetailModule {
    @ChildFragmentScoped
    @ContributesAndroidInjector(modules = [PhoneDialogModule::class])
    internal abstract fun fxDetailFragment(): FxDetailFragment

    @Binds
    @IntoMap
    @ViewModelKey(FxDetailViewModel::class)
    abstract fun bindFxDetailViewModel(fxDetailViewModel: FxDetailViewModel): ViewModel
}