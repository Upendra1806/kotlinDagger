package com.juliusbaer.premarket.ui.alerts

import androidx.lifecycle.ViewModel
import com.juliusbaer.premarket.core.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
internal abstract class UnderlyingAlertsModule {
    @Binds
    @IntoMap
    @ViewModelKey(UnderlyingAlertsViewModel::class)
    abstract fun bindUnderlyingAlertsViewModel(underlyingAlertsViewModel: UnderlyingAlertsViewModel): ViewModel
}