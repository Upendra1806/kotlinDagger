package com.juliusbaer.premarket.ui.company.warrants

import com.juliusbaer.premarket.core.di.ChildChildFragmentScoped
import com.juliusbaer.premarket.ui.warrants.WarrantsViewModelModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
internal abstract class CompanyWarrantsModule {
    @ChildChildFragmentScoped
    @ContributesAndroidInjector(modules=[WarrantsViewModelModule::class])
    internal abstract fun companyWarrantsFragment(): CompanyWarrantsFragment


}