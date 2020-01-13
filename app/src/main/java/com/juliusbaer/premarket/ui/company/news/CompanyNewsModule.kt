package com.juliusbaer.premarket.ui.company.news

import androidx.lifecycle.ViewModel
import com.juliusbaer.premarket.core.di.ChildChildFragmentScoped
import com.juliusbaer.premarket.core.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
internal abstract class CompanyNewsModule {
    @ChildChildFragmentScoped
    @ContributesAndroidInjector
    internal abstract fun companyNewsFragment(): CompanyNewsFragment

    @Binds
    @IntoMap
    @ViewModelKey(CompanyNewsVM::class)
    abstract fun bindCompanyNewsVM(companyNewsVM: CompanyNewsVM): ViewModel
}