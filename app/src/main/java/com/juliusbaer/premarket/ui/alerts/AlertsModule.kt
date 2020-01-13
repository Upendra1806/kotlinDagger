package com.juliusbaer.premarket.ui.alerts

import androidx.lifecycle.ViewModel
import com.juliusbaer.premarket.core.di.ChildFragmentScoped
import com.juliusbaer.premarket.core.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
internal abstract class AlertsModule {
    @ChildFragmentScoped
    @ContributesAndroidInjector
    internal abstract fun alertsFragment(): AlertsFragment

    @Binds
    @IntoMap
    @ViewModelKey(AlertsVM::class)
    abstract fun bindAlertsVM(alertsVM: AlertsVM): ViewModel
}