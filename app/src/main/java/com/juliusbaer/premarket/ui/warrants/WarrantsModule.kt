package com.juliusbaer.premarket.ui.warrants

import com.juliusbaer.premarket.core.di.ChildFragmentScoped
import com.juliusbaer.premarket.ui.phoneDialog.PhoneDialogModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
internal abstract class WarrantsModule {
    @ChildFragmentScoped
    @ContributesAndroidInjector(modules = [PhoneDialogModule::class, WarrantsViewModelModule::class])
    internal abstract fun warrantsFragment(): WarrantsFragment
}