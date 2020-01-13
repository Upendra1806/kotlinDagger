package com.juliusbaer.premarket.ui.phoneDialog

import androidx.lifecycle.ViewModel
import com.juliusbaer.premarket.core.di.ChildChildFragmentScoped
import com.juliusbaer.premarket.core.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
internal abstract class PhoneDialogModule {
    @ChildChildFragmentScoped
    @ContributesAndroidInjector
    internal abstract fun bindPhoneDialogFragment(): PhoneDialogFragment

    @Binds
    @IntoMap
    @ViewModelKey(PhoneDialogViewModel::class)
    abstract fun bindCustomDialogViewModel(customDialogViewModel: PhoneDialogViewModel): ViewModel
}