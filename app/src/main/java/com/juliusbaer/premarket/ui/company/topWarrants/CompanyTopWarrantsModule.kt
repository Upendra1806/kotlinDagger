package com.juliusbaer.premarket.ui.company.topWarrants

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
internal abstract class CompanyTopWarrantsModule {
    @ChildFragmentScoped
    @ContributesAndroidInjector(modules = [PhoneDialogModule::class])
    internal abstract fun companyTopWarrantsFragment(): CompanyTopWarrantsFragment

    @Binds
    @IntoMap
    @ViewModelKey(CompanyTopWarrantsVM::class)
    abstract fun bindTopCompanyVM(topCompanyVM: CompanyTopWarrantsVM): ViewModel
}