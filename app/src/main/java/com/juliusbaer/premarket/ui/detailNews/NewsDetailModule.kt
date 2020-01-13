package com.juliusbaer.premarket.ui.detailNews

import androidx.lifecycle.ViewModel
import com.juliusbaer.premarket.core.di.ChildFragmentScoped
import com.juliusbaer.premarket.core.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
internal abstract class NewsDetailModule {
    @ChildFragmentScoped
    @ContributesAndroidInjector
    internal abstract fun newsDetailFragment(): NewsDetailFragment

    @Binds
    @IntoMap
    @ViewModelKey(NewsDetailViewModel::class)
    abstract fun bindNewsDetailViewModel(newsDetailViewModel: NewsDetailViewModel): ViewModel
}