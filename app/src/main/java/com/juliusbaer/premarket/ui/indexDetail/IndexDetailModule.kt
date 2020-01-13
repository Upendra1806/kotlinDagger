package com.juliusbaer.premarket.ui.indexDetail

import androidx.lifecycle.ViewModel
import com.juliusbaer.premarket.core.di.ChildFragmentScoped
import com.juliusbaer.premarket.core.di.ViewModelKey
import com.juliusbaer.premarket.ui.company.news.CompanyNewsModule
import com.juliusbaer.premarket.ui.company.warrants.CompanyWarrantsModule
import com.juliusbaer.premarket.ui.indexDetail.actualIndex.IndexPerformanceModule
import com.juliusbaer.premarket.ui.phoneDialog.PhoneDialogModule
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
internal abstract class IndexDetailModule {
    @ChildFragmentScoped
    @ContributesAndroidInjector(modules = [
        IndexPerformanceModule::class,
        CompanyNewsModule::class,
        CompanyWarrantsModule::class,
        PhoneDialogModule::class
    ])
    internal abstract fun indexDetailFragment(): IndexDetailFragment

    @Binds
    @IntoMap
    @ViewModelKey(IndexDetailViewModel::class)
    abstract fun bindIndexDetailViewModel(indexDetailViewModel: IndexDetailViewModel): ViewModel
}