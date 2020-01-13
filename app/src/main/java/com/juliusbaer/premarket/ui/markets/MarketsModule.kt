package com.juliusbaer.premarket.ui.markets

import com.juliusbaer.premarket.core.di.ChildFragmentScoped
import com.juliusbaer.premarket.ui.markets.fx.FxModule
import com.juliusbaer.premarket.ui.markets.indices.MarketsIndicesModule
import com.juliusbaer.premarket.ui.phoneDialog.PhoneDialogModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
internal abstract class MarketsModule {
    @ChildFragmentScoped
    @ContributesAndroidInjector(modules = [FxModule::class, MarketsIndicesModule::class, PhoneDialogModule::class])
    internal abstract fun marketsFragment(): MarketsFragment
}