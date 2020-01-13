package com.juliusbaer.premarket.ui.settings

import androidx.lifecycle.ViewModel
import com.juliusbaer.premarket.core.di.ChildFragmentScoped
import com.juliusbaer.premarket.core.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
internal abstract class SettingsModule {
    @ChildFragmentScoped
    @ContributesAndroidInjector
    internal abstract fun settingsFragment(): SettingsFragment

    @Binds
    @IntoMap
    @ViewModelKey(SettingsVM::class)
    abstract fun bindSettingsVM(settingsVM: SettingsVM): ViewModel
}