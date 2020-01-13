package com.juliusbaer.premarket.ui.underluings

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
internal abstract class UnderlyingsModule {
    @ChildFragmentScoped
    @ContributesAndroidInjector(modules=[PhoneDialogModule::class, UnderlyingListModule::class])
    internal abstract fun underlyingsFragment(): UnderlyingsFragment

    @Binds
    @IntoMap
    @ViewModelKey(UnderlyingsViewModel::class)
    abstract fun bindUnderlyingsViewModel(underlyingsViewModel: UnderlyingsViewModel): ViewModel
}