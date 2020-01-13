package com.juliusbaer.premarket.ui.company

import androidx.lifecycle.ViewModel
import com.juliusbaer.premarket.core.di.ChildFragmentScoped
import com.juliusbaer.premarket.core.di.ViewModelKey
import com.juliusbaer.premarket.ui.company.news.CompanyNewsModule
import com.juliusbaer.premarket.ui.company.performance.PerformanceModule
import com.juliusbaer.premarket.ui.company.warrants.CompanyWarrantsModule
import com.juliusbaer.premarket.ui.phoneDialog.PhoneDialogModule
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
internal abstract class CompanyModule {
    @ChildFragmentScoped
    @ContributesAndroidInjector(modules = [
        PerformanceModule::class,
        CompanyNewsModule::class,
        CompanyWarrantsModule::class,
        PhoneDialogModule::class])
    internal abstract fun companyFragment(): CompanyFragment

    @Binds
    @IntoMap
    @ViewModelKey(CompanyVM::class)
    abstract fun bindCompanyVM(companyVM: CompanyVM): ViewModel
}