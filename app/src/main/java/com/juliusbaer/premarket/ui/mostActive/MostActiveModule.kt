package com.juliusbaer.premarket.ui.mostActive

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
internal abstract class MostActiveModule {
    @ChildFragmentScoped
    @ContributesAndroidInjector(modules = [PhoneDialogModule::class])
    internal abstract fun mostActiveFragment(): MostActiveFragment

    @Binds
    @IntoMap
    @ViewModelKey(MostActiveViewModel::class)
    abstract fun bindMostActiveViewModel(mostActiveViewModel: MostActiveViewModel): ViewModel
}