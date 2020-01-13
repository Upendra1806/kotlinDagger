package com.juliusbaer.premarket.ui.disclaimer

import androidx.lifecycle.ViewModel
import com.juliusbaer.premarket.core.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
internal abstract class DisclaimerModule {
    @Binds
    @IntoMap
    @ViewModelKey(DisclaimerViewModel::class)
    abstract fun bindDisclaimerViewModel(DisclaimerViewModel: DisclaimerViewModel): ViewModel
}