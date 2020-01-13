package com.juliusbaer.premarket.ui.detailWarrant

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
internal abstract class WarrantDetailModule {
    @ChildFragmentScoped
    @ContributesAndroidInjector(modules = [PhoneDialogModule::class])
    internal abstract fun warrantsDetailNFragment(): WarrantDetailFragment

    @Binds
    @IntoMap
    @ViewModelKey(WarrantsDetailViewModel::class)
    abstract fun bindWarrantsDetailViewModel(warrantsDetailViewModel: WarrantsDetailViewModel): ViewModel
}