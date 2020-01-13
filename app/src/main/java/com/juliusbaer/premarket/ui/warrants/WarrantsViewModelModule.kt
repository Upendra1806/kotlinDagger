package com.juliusbaer.premarket.ui.warrants

import androidx.lifecycle.ViewModel
import com.juliusbaer.premarket.core.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
internal abstract class WarrantsViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(WarrantsVM::class)
    abstract fun bindWarrantsVM(warrantsVM: WarrantsVM): ViewModel
}