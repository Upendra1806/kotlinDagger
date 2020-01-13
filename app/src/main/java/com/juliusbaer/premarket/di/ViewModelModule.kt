package com.juliusbaer.premarket.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.juliusbaer.premarket.core.di.ViewModelKey
import com.juliusbaer.premarket.core.viewmodel.ViewModelFactory
import com.juliusbaer.premarket.ui.base.UserInfoViewModel
import com.juliusbaer.premarket.ui.promotion.PromotionViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(PromotionViewModel::class)
    abstract fun bindPromotionViewModel(promotionViewModel: PromotionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserInfoViewModel::class)
    abstract fun bindUserInfoViewModel(userInfoViewModel: UserInfoViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
