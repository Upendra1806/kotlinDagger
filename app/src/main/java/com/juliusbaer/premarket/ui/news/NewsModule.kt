package com.juliusbaer.premarket.ui.news

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
internal abstract class NewsModule {
    @ChildFragmentScoped
    @ContributesAndroidInjector(modules = [PhoneDialogModule::class])
    internal abstract fun newsFragment(): NewsFragment

    @Binds
    @IntoMap
    @ViewModelKey(NewsVM::class)
    abstract fun bindNewsVM(newsVM: NewsVM): ViewModel
}