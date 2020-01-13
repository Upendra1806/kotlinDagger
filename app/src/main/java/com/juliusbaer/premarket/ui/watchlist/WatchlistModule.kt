package com.juliusbaer.premarket.ui.watchlist

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
internal abstract class WatchlistModule {
    @ChildFragmentScoped
    @ContributesAndroidInjector(modules = [PhoneDialogModule::class])
    internal abstract fun watchlistFragment(): WatchlistFragment

    @Binds
    @IntoMap
    @ViewModelKey(WatchlistVM::class)
    abstract fun bindWatchlistVM(watchlistVM: WatchlistVM): ViewModel
}