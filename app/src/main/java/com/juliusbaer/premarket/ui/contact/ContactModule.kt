package com.juliusbaer.premarket.ui.contact

import com.juliusbaer.premarket.core.di.ChildFragmentScoped
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class ContactModule {
    @ChildFragmentScoped
    @ContributesAndroidInjector
    internal abstract fun contactFragment(): ContactFragment
}